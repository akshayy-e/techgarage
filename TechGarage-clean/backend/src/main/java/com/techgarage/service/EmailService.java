package com.techgarage.service;

import com.techgarage.entity.User;

public interface EmailService {
    void sendPasswordResetEmail(User user, String token);
}
