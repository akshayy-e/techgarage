package com.techgarage.service.impl;

import com.techgarage.entity.User;
import com.techgarage.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:false}") private boolean enabled;
    @Value("${app.mail.from:noreply@techgarage.local}") private String from;
    @Value("${app.frontend-url:http://localhost:5173}") private String frontendUrl;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String link = frontendUrl.replaceAll("/$", "") + "/verify-email?token=" + token;
        send(user.getEmail(), "Verify your TechGarage email", "Hi " + user.getName() + ",\n\nVerify your email to secure your TechGarage account:\n" + link + "\n\nThis link expires in 24 hours.\n\nTechGarage");
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        String link = frontendUrl.replaceAll("/$", "") + "/reset-password?token=" + token;
        send(user.getEmail(), "Reset your TechGarage password", "Hi " + user.getName() + ",\n\nReset your TechGarage password here:\n" + link + "\n\nThis link expires in 30 minutes. If you did not request this, ignore this email.\n\nTechGarage");
    }

    private void send(String to, String subject, String text) {
        if (!enabled) {
            log.info("Email delivery disabled. Would send '{}' to {}", subject, to);
            return;
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
