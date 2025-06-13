package org.aibles.authenservice.facade.impl;

import org.aibles.authenservice.dto.request.*;
import org.aibles.authenservice.dto.response.BaseResponse;
import org.aibles.authenservice.dto.response.LoginResponse;
import org.aibles.authenservice.dto.response.SignupResponse;
import org.aibles.authenservice.entity.Account;
import org.aibles.authenservice.entity.User;
import org.aibles.authenservice.exception.*;
import org.aibles.authenservice.facade.AuthService;
import org.aibles.authenservice.repository.AccountRepository;
import org.aibles.authenservice.repository.AccountRoleRepository;
import org.aibles.authenservice.repository.RoleRepository;
import org.aibles.authenservice.repository.UserRepository;
import org.aibles.authenservice.service.AccountRoleService;
import org.aibles.authenservice.service.AccountService;
import org.aibles.authenservice.service.RoleService;
import org.aibles.authenservice.service.UserService;
import org.aibles.authenservice.utill.JwtUtil;
import org.aibles.authenservice.utill.OTPGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private RedisTemplate<String, String> otpUsedStore;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRoleService accountRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AccountRoleRepository accountRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final String OTP_PREFIX = "OTP:";
    private static final String OTP_FORGOT_PASSWORD = "OTP_FORGOT_PASSWORD:";
    private static final String OTP_USED_PREFIX = "OTP_USED:";
    private static final String LOGIN_FAILED = "LOGIN_FAILED";
    private static final int MAX_LOGIN_FAILED = 5;
    private static final long LOCK_DURATION_MINUTES = 2;
    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 days

    @Transactional
    public BaseResponse<SignupResponse> registerUser(RegisterUserRequest request) {
        log.info("Registering user with email: {} and username: {}", request.getEmail(), request.getUsername());
        try {
            User savedUser = userService.createUser(request.getEmail());
            log.debug("User created with ID: {}", savedUser.getId());

            Account savedAccount = accountService.createAccount(
                    request.getUsername(),
                    request.getPassword(),
                    savedUser.getId(),
                    false,
                    true
            );
            log.debug("Account created with ID: {}", savedAccount.getId());

            var userRole = roleService.findOrCreateRole("USER");
            accountRoleService.assignRole(savedAccount.getId(), userRole.getId());
            log.debug("Assigned role USER to account ID: {}", savedAccount.getId());

            SignupResponse response = new SignupResponse(request.getEmail(), request.getUsername());
            log.info("User registration successful for username: {}", request.getUsername());
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), response);
        } catch (Exception e) {
            log.error("Failed to register user with email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public void sendOTP(SendOtpRequest request) {
        log.info("Sending OTP to email: {}", request.getEmail());
        try {
            userService.getUserByEmail(request.getEmail());
            String redisKey = getOtpRedisKey(request.getEmail());
            String otpCode = OTPGenerator.generateOtp();
            redisTemplate.opsForValue().set(redisKey, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
            log.debug("Stored OTP in Redis with key: {} and value: {}", redisKey, otpCode);

            sendEmail(request.getEmail(), otpCode);
            log.info("OTP sent successfully to email: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP to email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    public BaseResponse<String> active(ActiveAccountRequest request) {
        log.info("Activating account for email: {} with OTP: {}", request.getEmail(), request.getOtp());
        try {
            if (!isOtpValid(request.getEmail(), request.getOtp())) {
                log.warn("Invalid OTP for email: {}", request.getEmail());
                throw new OTPInvalidException();
            }
            if (isOtpUsed(request.getEmail(), request.getOtp())) {
                log.warn("OTP already used for email: {}", request.getEmail());
                throw new OTPIsUsedException();
            }

            Account account = getAccountByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("Account not found for email: {}", request.getEmail());
                        return new EmailNotFoundException(request.getEmail());
                    });
            accountService.activateAccount(account);
            log.debug("Activated account ID: {}", account.getId());

            String usedKey = getOtpUsedRedisKey(request.getEmail(), request.getOtp());
            otpUsedStore.opsForValue().set(usedKey, "USED", OTP_EXPIRY_MINUTES * 2, TimeUnit.MINUTES);
            log.debug("Marked OTP as used in Redis with key: {} for {} minutes", usedKey, OTP_EXPIRY_MINUTES * 2);

            log.info("Account activation successful for email: {}", request.getEmail());
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "ACTIVATE SUCCESS");
        } catch (Exception e) {
            log.error("Failed to activate account for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public BaseResponse<LoginResponse> login(LoginRequest request) {
        log.info("Processing login request for username: {}", request.getUsername());
        try {
            Account account = accountRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> {
                        log.error("Username not found: {}", request.getUsername());
                        return new UsernameNotFoundException(request.getUsername());
                    });

            String loginAttemptKey = LOGIN_FAILED + request.getUsername();
            String attemptCountStr = redisTemplate.opsForValue().get(loginAttemptKey);
            int attemptCount = attemptCountStr != null ? Integer.parseInt(attemptCountStr) : 0;

            if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
                attemptCount++;
                redisTemplate.opsForValue().set(loginAttemptKey, String.valueOf(attemptCount), LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
                log.warn("Invalid password for username: {}. Attempt count: {}", request.getUsername(), attemptCount);

                if (attemptCount >= MAX_LOGIN_FAILED) {
                    accountService.lockAccount(account);
                    redisTemplate.delete(loginAttemptKey);
                    log.error("Account locked for username: {} due to too many failed attempts", request.getUsername());
                    throw new AccountIsLockedException();
                }
                throw new PasswordInvalidException();
            }

            if (!account.getActivated()) {
                log.warn("Account not activated for username: {}", request.getUsername());
                throw new AccountIsActiveException();
            }

            String accessToken = jwtUtil.generateAccessToken(account.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(account.getUsername());
            if (attemptCount > 0) {
                redisTemplate.delete(loginAttemptKey);
                log.debug("Cleared login attempt count for username: {}", request.getUsername());
            }
            long accessTokenExpiration = (System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION) / 1000;
            long refreshTokenExpiration = (System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION) / 1000;

            LoginResponse loginResponse = new LoginResponse(
                    accessToken,
                    refreshToken,
                    accessTokenExpiration,
                    refreshTokenExpiration
            );
            log.info("Login successful for username: {}", request.getUsername());
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), loginResponse);
        } catch (Exception e) {
            log.error("Failed to login for username: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public BaseResponse<String> changePassword(ChangePasswordRequest request, String userId) {
        log.info("Changing password for userId: {}", userId);
        try {
            Account account = accountRepository.findByUsername(userId)
                    .orElseThrow(() -> {
                        log.error("User not found for userId: {}", userId);
                        return new UserNotFoundException(userId);
                    });

            if (Objects.equals(request.getNewPassword(), request.getOldPassword())) {
                log.warn("New password is the same as old password for userId: {}", userId);
                throw new PasswordSimilarException();
            }
            if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
                log.warn("Confirm password does not match for userId: {}", userId);
                throw new PasswordConfirmNotMatchException();
            }

            accountService.updatePassword(account, request.getNewPassword());
            log.info("Password changed successfully for userId: {}", userId);
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "CHANGE PASSWORD SUCCESS");
        } catch (Exception e) {
            log.error("Failed to change password for userId: {}. Error: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    public BaseResponse<String> resetPassword(ResetPasswordRequest request) {
        log.info("Resetting password for email: {}", request.getEmail());
        try {
            User user = userService.getUserByEmail(request.getEmail());
            String otpKey = OTP_FORGOT_PASSWORD + request.getEmail();
            String storedOtp = redisTemplate.opsForValue().get(otpKey);
            if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
                log.warn("Invalid OTP for email: {}", request.getEmail());
                throw new OTPInvalidException();
            }
            if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
                log.warn("Confirm password does not match for email: {}", request.getEmail());
                throw new PasswordConfirmNotMatchException();
            }

            Account account = accountRepository.findByUserId(user.getId())
                    .orElseThrow(() -> {
                        log.error("Account not found for email: {}", request.getEmail());
                        return new UserNotFoundException(request.getEmail());
                    });
            accountService.updatePassword(account, request.getNewPassword());
            redisTemplate.delete(otpKey);
            log.info("Password reset successfully for email: {}", request.getEmail());
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "Password reset successfully");
        } catch (Exception e) {
            log.error("Failed to reset password for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public BaseResponse<String> forgotPassword(ForgotPasswordRequest request) {
        log.info("Processing forgot password request for email: {}", request.getEmail());
        try {
            User user = userService.getUserByEmail(request.getEmail());
            String otp = OTPGenerator.generateOtp();
            String otpKey = OTP_FORGOT_PASSWORD + request.getEmail();
            redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
            log.debug("Stored OTP for forgot password in Redis with key: {} and value: {}", otpKey, otp);

            sendEmail(request.getEmail(), otp);
            log.info("OTP sent successfully for forgot password to email: {}", request.getEmail());
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "OTP sent to email");
        } catch (Exception e) {
            log.error("Failed to process forgot password for email: {}. Error: {}", request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @Transactional
    @Override
    public BaseResponse<LoginResponse> loginWithGoogle(String email) {
        log.info("Processing Google login for email: {}", email);
        try {
            Optional<User> userOpt = userService.findByEmail(email);
            User user;
            Account account;

            if (userOpt.isEmpty()) {
                user = userService.createUser(email);
                log.debug("Created new user with email: {}", email);
                account = accountService.createAccount(
                        email,
                        "google-auth-" + System.currentTimeMillis(),
                        user.getId(),
                        true,
                        true
                );
                log.debug("Created new account with ID: {}", account.getId());
                var userRole = roleService.findOrCreateRole("USER");
                accountRoleService.assignRole(account.getId(), userRole.getId());
                log.debug("Assigned role USER to account ID: {}", account.getId());
            } else {
                user = userOpt.get();
                account = accountRepository.findByUserId(user.getId())
                        .orElseThrow(() -> {
                            log.error("Account not found for email: {}", email);
                            return new UsernameNotFoundException(email);
                        });
            }

            if (!account.getIsLocked()) {
                log.warn("Account is locked for email: {}", email);
                throw new AccountIsLockedException();
            }
            if (!account.getActivated()) {
                log.warn("Account not activated for email: {}", email);
                throw new AccountIsActiveException();
            }

            long now = System.currentTimeMillis();
            String accessToken = jwtUtil.generateAccessToken(account.getUsername());
            String refreshToken = jwtUtil.generateRefreshToken(account.getUsername());
            long accessTokenExpiration = (now + ACCESS_TOKEN_EXPIRATION) / 1000;
            long refreshTokenExpiration = (now + REFRESH_TOKEN_EXPIRATION) / 1000;

            LoginResponse loginResponse = new LoginResponse(
                    accessToken,
                    refreshToken,
                    accessTokenExpiration,
                    refreshTokenExpiration
            );
            log.info("Google login successful for email: {}", email);
            return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), loginResponse);
        } catch (Exception e) {
            log.error("Failed to process Google login for email: {}. Error: {}", email, e.getMessage(), e);
            throw e;
        }
    }

    private boolean isOtpValid(String email, String otpCode) {
        String otpKey = getOtpRedisKey(email);
        String storedOtp = redisTemplate.opsForValue().get(otpKey);
        boolean isValid = storedOtp != null && storedOtp.equals(otpCode);
        log.debug("Checking OTP validity for email: {}. Is valid: {}", email, isValid);
        return isValid;
    }

    private boolean isOtpUsed(String email, String otpCode) {
        String usedKey = getOtpUsedRedisKey(email, otpCode);
        String isUsed = otpUsedStore.opsForValue().get(usedKey);
        boolean used = isUsed != null && isUsed.equals("USED");
        log.debug("Checking if OTP is used for email: {}. Is used: {}", email, used);
        return used;
    }

    private Optional<Account> getAccountByEmail(String email) {
        Optional<User> user = userService.findByEmail(email);
        Optional<Account> account = user.isPresent() ? accountRepository.findByUserId(user.get().getId()) : Optional.empty();
        log.debug("Retrieving account for email: {}. Found: {}", email, account.isPresent());
        return account;
    }

    private String getOtpRedisKey(String email) {
        String key = OTP_PREFIX + email;
        log.debug("Generated OTP Redis key: {}", key);
        return key;
    }

    private String getOtpUsedRedisKey(String email, String otpCode) {
        String key = OTP_USED_PREFIX + email + ":" + otpCode;
        log.debug("Generated OTP used Redis key: {}", key);
        return key;
    }

    private void sendEmail(String toEmail, String otpCode) {
        log.info("Sending email with OTP to: {}", toEmail);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Mã xác thực OTP");
            message.setText("Mã xác thực OTP của bạn là: " + otpCode + "\nMã có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút.");
            mailSender.send(message);
            log.debug("Email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", toEmail, e.getMessage(), e);
            throw e;
        }
    }
}