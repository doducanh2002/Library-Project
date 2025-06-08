//package org.aibles.authenservice.service.impl;
//
//import org.aibles.authenservice.dto.request.*;
//import org.aibles.authenservice.dto.response.BaseResponse;
//import org.aibles.authenservice.dto.response.LoginResponse;
//import org.aibles.authenservice.dto.response.SignupResponse;
//import org.aibles.authenservice.entity.Account;
//import org.aibles.authenservice.entity.AccountRole;
//import org.aibles.authenservice.entity.Role;
//import org.aibles.authenservice.entity.User;
//import org.aibles.authenservice.exception.*;
//import org.aibles.authenservice.repository.AccountRepository;
//import org.aibles.authenservice.repository.AccountRoleRepository;
//import org.aibles.authenservice.repository.RoleRepository;
//import org.aibles.authenservice.repository.UserRepository;
//import org.aibles.authenservice.facade.AuthService;
//import org.aibles.authenservice.utill.JwtUtil;
//import org.aibles.authenservice.utill.OTPGenerator;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import javax.security.auth.login.AccountNotFoundException;
//import java.util.HashMap;
//import java.util.Objects;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//@Service
//public class AuthServiceImpl implements AuthService {
//
//    @Autowired
//    private RedisTemplate<String, String> redisTemplate;
//
//    @Autowired
//    private RedisTemplate<String, Boolean> otpUsedStore;
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Autowired
//    private AccountRepository accountRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private AccountRoleRepository accountRoleRepository;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    private static final int OTP_EXPIRY_MINUTES = 5;
//    private static final String OTP_PREFIX = "OTP:";
//    private static final String OTP_FORGOT_PASSWORD = "OTP_FORGOT_PASSWORD:";
//
//    private static final String OTP_USED_PREFIX = "OTP_USED:";
//;
//    private static final String LOGIN_FAILED = "LOGIN_FAILED";
//    private static final int MAX_LOGIN_FAILED = 5;
//    private static final long LOCK_DURATION_MINUTES = 2;
//
//    private static final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 15; // 15 phút
//    private static final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7 ngày
//
//    @Transactional
//    public BaseResponse<SignupResponse> registerUser(RegisterUserRequest request) {
//
//        if (accountRepository.existsByUsername(request.getUsername())) {
//            throw new UsernameAlreadyExistsException();
//        }
//        if (request.getPassword() == null || request.getPassword().length() < 8) {
//            throw new PasswordInvalidException();
//        }
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new EmailAlreadyExistsException();
//        }
//
//        User user = new User();
//        user.setEmail(request.getEmail());
//        User savedUser = userRepository.save(user);
//
//        Account account = new Account();
//        account.setUsername(request.getUsername());
//        account.setPassword(passwordEncoder.encode(request.getPassword()));
//        account.setActivated(false);
//        account.setIsLocked(true);
//        account.setUserId(savedUser.getId());
//        Account savedAccount = accountRepository.save(account);
//
//        Role userRole = roleRepository.findByName("USER")
//                .orElseGet(() -> {
//                    Role newRole = new Role();
//                    newRole.setName("USER");
//                    return roleRepository.save(newRole);
//                });
//
//        AccountRole accountRole = new AccountRole();
//        accountRole.setAccountId(savedAccount.getId());
//        accountRole.setRoleId(userRole.getId());
//        accountRoleRepository.save(accountRole);
//
//        SignupResponse response = new SignupResponse(request.getEmail(), request.getUsername());
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), response);
//    }
//
//    public void sendOTP(SendOtpRequest request) {
//        Optional<User> user = userRepository.findByEmail(request.getEmail());
//        if (user.isEmpty()) {
//            throw new EmailNotFoundException(request.getEmail());
//        }
//
//        String redisKey = getOtpRedisKey(request.getEmail());
//        String otpCode = OTPGenerator.generateOtp();
//        redisTemplate.opsForValue().set(redisKey, otpCode, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
//
//        sendEmail(request.getEmail(), otpCode);
//    }
//
//    public BaseResponse<String> active(ActiveAccountRequest request) {
//        if (!isOtpValid(request.getEmail(), request.getOtp())) {
//            throw new OTPInvalidException();
//        }
//
//        if (isOtpUsed(request.getEmail(), request.getOtp())) {
//            throw new OTPIsUsedException();
//        }
//
//        Account account = getAccountByEmail(request.getEmail())
//                .orElseThrow(() -> new EmailNotFoundException(request.getEmail()));
//
//        account.setActivated(true);
//        accountRepository.save(account);
//        String usedKey = getOtpUsedRedisKey(request.getEmail(), request.getOtp());
//        otpUsedStore.opsForValue().set(usedKey, true);
//
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "ACTIVATE SUCCESS");
//    }
//
//    @Override
//    public BaseResponse<LoginResponse> login(LoginRequest request) {
//        Optional<Account> accountOpt = accountRepository.findByUsername(request.getUsername());
//        if (accountOpt.isEmpty()) {
//            throw new UsernameNotFoundException(request.getUsername());
//        }
//
//        Account account = accountOpt.get();
////        if (account.getIsLocked()) {
////            throw new AccountIsLockedException();
////        }
//
//        String loginAttemptKey = LOGIN_FAILED + request.getUsername();
//        String attemptCountStr = redisTemplate.opsForValue().get(loginAttemptKey);
//        int attemptCount = attemptCountStr != null ? Integer.parseInt(attemptCountStr) : 0;
//
//        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
//            attemptCount++;
//            redisTemplate.opsForValue().set(loginAttemptKey, String.valueOf(attemptCount), LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
//
//            if (attemptCount >= MAX_LOGIN_FAILED) {
//                account.setIsLocked(false);
//                accountRepository.save(account);
//                redisTemplate.delete(loginAttemptKey);
//                throw new AccountIsLockedException();
//            }
//            throw new PasswordInvalidException();
//        }
//
//        if (!account.getActivated()) {
//            throw new AccountIsActiveException();
//        }
//
//        String accessToken = jwtUtil.generateAccessToken(account.getUsername());
//        String refreshToken = jwtUtil.generateRefreshToken(account.getUsername());
//        if(attemptCount > 0){
//            redisTemplate.delete(loginAttemptKey);
//        }
//        long accessTokenExpiration = (System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION) / 1000;
//        long refreshTokenExpiration = (System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION) / 1000;
//
//        LoginResponse loginResponse = new LoginResponse(
//                accessToken,
//                refreshToken,
//                accessTokenExpiration,
//                refreshTokenExpiration
//        );
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), loginResponse);
//    }
//
//    @Override
//    public BaseResponse<String> changePassword(ChangePasswordRequest request, String userId) {
//        var account = accountRepository.findByUsername(userId)
//                .orElseThrow(() -> {
//                    throw new UserNotFoundException(userId);
//                });
//        if (Objects.equals(request.getNewPassword(), request.getOldPassword())) {
//            throw new PasswordSimilarException();
//        }
//
//        if(!Objects.equals(request.getNewPassword(), request.getConfirmPassword())){
//            throw new PasswordConfirmNotMatchException();
//        }
//        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        accountRepository.save(account);
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "CHANGE PASSWORD SUCCESS");
//
//    }
//
//    public BaseResponse<String> resetPassword(ResetPasswordRequest request) {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));
//        String otpKey = OTP_FORGOT_PASSWORD + request.getEmail();
//        String storedOtp = redisTemplate.opsForValue().get(otpKey);
//        if (storedOtp == null || !storedOtp.equals(request.getOtp())) {
//            throw new OTPInvalidException();
//        }
//        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
//            throw new PasswordConfirmNotMatchException();
//        }
//        Account account = accountRepository.findByUserId(user.getId())
//                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));
//        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
//        accountRepository.save(account);
//        redisTemplate.delete(otpKey);
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "Password reset successfully");
//    }
//
//    @Override
//    public BaseResponse<String> forgotPassword(ForgotPasswordRequest request) {
//        User user = userRepository.findByEmail(request.getEmail())
//                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));
//
//        String otp = OTPGenerator.generateOtp();
//        String otpKey = OTP_FORGOT_PASSWORD + request.getEmail();
//        redisTemplate.opsForValue().set(otpKey, otp, OTP_EXPIRY_MINUTES, TimeUnit.MINUTES);
//        sendEmail(request.getEmail(), otp);
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), "OTP sent to email");
//    }
//
//    @Transactional
//    @Override
//    public BaseResponse<LoginResponse> loginWithGoogle(String email) {
//        // Kiểm tra email đã tồn tại chưa
//        Optional<User> userOpt = userRepository.findByEmail(email);
//        User user;
//        Account account;
//
//        if (userOpt.isEmpty()) {
//            // Tạo user mới
//            user = new User();
//            user.setEmail(email);
//            user = userRepository.save(user);
//
//            // Tạo account
//            account = new Account();
//            account.setUsername(email); // Dùng email làm username
//            account.setPassword(passwordEncoder.encode("google-auth-" + System.currentTimeMillis()));
//            account.setActivated(true);
//            account.setIsLocked(true);
//            account.setUserId(user.getId());
//            account = accountRepository.save(account);
//
//            // Gán role USER
//            Role userRole = roleRepository.findByName("USER")
//                    .orElseGet(() -> {
//                        Role newRole = new Role();
//                        newRole.setName("USER");
//                        return roleRepository.save(newRole);
//                    });
//
//            AccountRole accountRole = new AccountRole();
//            accountRole.setAccountId(account.getId());
//            accountRole.setRoleId(userRole.getId());
//            accountRoleRepository.save(accountRole);
//        } else {
//            user = userOpt.get();
//            account = accountRepository.findByUserId(user.getId())
//                    .orElseThrow(() -> new UsernameNotFoundException(email));
//        }
//
//        // Kiểm tra khóa tài khoản
//        if (!account.getIsLocked()) {
//            throw new AccountIsLockedException();
//        }
//
//        // Kiểm tra kích hoạt
//        if (!account.getActivated()) {
//            throw new AccountIsActiveException();
//        }
//
//        // Tạo token
//        long now = System.currentTimeMillis();
//        String accessToken = jwtUtil.generateAccessToken(account.getUsername());
//        String refreshToken = jwtUtil.generateRefreshToken(account.getUsername());
//
//        // Tính expiration (epoch seconds)
//        long accessTokenExpiration = (now + ACCESS_TOKEN_EXPIRATION) / 1000;
//        long refreshTokenExpiration = (now + REFRESH_TOKEN_EXPIRATION) / 1000;
//
//        LoginResponse loginResponse = new LoginResponse(
//                accessToken,
//                refreshToken,
//                accessTokenExpiration,
//                refreshTokenExpiration
//        );
//        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), loginResponse);
//    }
//
//
//    private boolean isOtpValid(String email, String otpCode) {
//        String otpKey = getOtpRedisKey(email);
//        String storedOtp = redisTemplate.opsForValue().get(otpKey);
//        return storedOtp != null && storedOtp.equals(otpCode);
//    }
//
//    private boolean isOtpUsed(String email, String otpCode) {
//        String usedKey = getOtpUsedRedisKey(email, otpCode);
//        Boolean isUsed = otpUsedStore.opsForValue().get(usedKey);
//        return isUsed != null && isUsed;
//    }
//
//    private Optional<Account> getAccountByEmail(String email) {
//        Optional<User> user = userRepository.findByEmail(email);
//        return user.isPresent() ? accountRepository.findByUserId(user.get().getId()) : Optional.empty();
//    }
//
//    private String getOtpRedisKey(String gmail) {
//        return OTP_PREFIX + gmail;
//    }
//
//    private String getOtpUsedRedisKey(String gmail, String otpCode) {
//        return OTP_USED_PREFIX + gmail + ":" + otpCode;
//    }
//
//    private void sendEmail(String toEmail, String otpCode) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(toEmail);
//        message.setSubject("Mã xác thực OTP");
//        message.setText("Mã xác thực OTP của bạn là: " + otpCode + "\nMã có hiệu lực trong " + OTP_EXPIRY_MINUTES + " phút.");
//        mailSender.send(message);
//    }
//}