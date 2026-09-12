package com.techgarage.service;

import com.techgarage.dto.auth.AuthResponse;
import com.techgarage.dto.auth.LoginRequest;
import com.techgarage.dto.auth.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
