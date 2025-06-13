package org.aibles.authenservice.facade;

import org.aibles.authenservice.dto.request.*;
import org.aibles.authenservice.dto.response.BaseResponse;
import org.aibles.authenservice.dto.response.LoginResponse;
import org.aibles.authenservice.dto.response.SignupResponse;

public interface AuthService {

    BaseResponse<SignupResponse> registerUser(RegisterUserRequest request);
    void sendOTP(SendOtpRequest email);
    BaseResponse<String> active(ActiveAccountRequest request);
    BaseResponse<LoginResponse> login(LoginRequest request);
    BaseResponse<String> changePassword(ChangePasswordRequest request, String accountId);
    BaseResponse<String> resetPassword(ResetPasswordRequest request);
    BaseResponse<String> forgotPassword(ForgotPasswordRequest request);
    BaseResponse<LoginResponse> loginWithGoogle(String email);
}
