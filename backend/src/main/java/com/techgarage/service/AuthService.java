package com.techgarage.service;

import com.techgarage.dto.auth.AuthResponse;
import com.techgarage.dto.auth.LoginRequest;
import com.techgarage.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void verifyEmail(String token);
    void requestPasswordReset(String email);
    void resendVerification(String email);
    void resetPassword(String token, String newPassword);
}
