package org.aibles.authenservice.controller;

import org.aibles.authenservice.dto.request.*;
import org.aibles.authenservice.dto.response.BaseResponse;
import org.aibles.authenservice.dto.response.LoginResponse;
import org.aibles.authenservice.dto.response.SignupResponse;
import org.aibles.authenservice.facade.AuthService;
import org.aibles.authenservice.utill.JwtUtil;
import org.aibles.authenservice.utill.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<SignupResponse> register(@RequestBody @Validated RegisterUserRequest request) {
        log.info("Received register request for username: {}", request.getUsername());
        return authService.registerUser(request);
    }

    @PostMapping("/sendotp")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<SignupResponse> sendOTP(@RequestBody @Validated SendOtpRequest request) {
        log.info("Received send OTP request for email: {}", request.getEmail());
        authService.sendOTP(request);
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(), null);
    }

    @PostMapping("/active")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<String> active(@RequestBody @Validated ActiveAccountRequest request) {
        log.info("Received active account request for email: {}", request.getEmail());
        return authService.active(request);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<LoginResponse> login(@RequestBody @Validated LoginRequest request) {
        log.info("Received login request for username: {}", request.getUsername());
        return authService.login(request);
    }

    @GetMapping("/jwk/token")
    @ResponseStatus(HttpStatus.OK)
    public String getJwks() {
        log.info("Received request for JWK token");
        String jwks = "{\"keys\":[" + jwtUtil.getJwk() + "]}";
        log.info("JWK token response: {}", jwks);
        return jwks;
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<String> change(@RequestBody @Validated ChangePasswordRequest request) {
        String userId = SecurityUtil.getCredential();
        log.info("Received change password request for userId: {}", userId);
        return authService.changePassword(request, userId);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<String> forgotPassword(@RequestBody @Validated ForgotPasswordRequest request) {
        log.info("Received forgot password request for email: {}", request.getEmail());
        return authService.forgotPassword(request);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<String> resetPassword(@RequestBody @Validated ResetPasswordRequest request) {
        log.info("Received reset password request for email: {}", request.getEmail());
        return authService.resetPassword(request);
    }
}