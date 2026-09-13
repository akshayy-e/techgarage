package com.techgarage.service.impl;

import com.techgarage.dto.auth.AuthResponse;
import com.techgarage.dto.auth.LoginRequest;
import com.techgarage.dto.auth.RegisterRequest;
import com.techgarage.entity.FreelancerProfile;
import com.techgarage.entity.Role;
import com.techgarage.entity.User;
import com.techgarage.exception.DuplicateResourceException;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.repository.UserRepository;
import com.techgarage.exception.BadRequestException;
import com.techgarage.service.EmailService;
import com.techgarage.entity.EmailVerificationToken;
import com.techgarage.repository.EmailVerificationTokenRepository;
import com.techgarage.repository.PasswordResetTokenRepository;
import com.techgarage.entity.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.UUID;
import com.techgarage.security.CustomUserDetailsService;
import com.techgarage.security.JwtUtil;
import com.techgarage.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmailService emailService;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @org.springframework.beans.factory.annotation.Value("${app.auth.require-email-verification:false}")
    private boolean requireEmailVerification;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be created through public registration");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .phone(request.getPhone())
                .enabled(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        String verificationToken = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                .user(user).token(verificationToken).expiresAt(LocalDateTime.now().plusHours(24)).build());
        emailService.sendVerificationEmail(user, verificationToken);

        if (user.getRole() == Role.FREELANCER) {
            FreelancerProfile profile = FreelancerProfile.builder()
                    .user(user)
                    .bio("")
                    .skills("")
                    .experienceYears(0)
                    .hourlyRate(0.0)
                    .build();
            freelancerProfileRepository.save(profile);
        }

        if (requireEmailVerification && !user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

        if (requireEmailVerification && !user.isEmailVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public void verifyEmail(String token) {
        EmailVerificationToken verification = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token"));
        if (verification.getExpiresAt().isBefore(LocalDateTime.now()) || verification.isUsed()) {
            throw new BadRequestException("Invalid or expired verification token");
        }
        User user = verification.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        verification.setUsed(true);
        emailVerificationTokenRepository.save(verification);
    }

    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            passwordResetTokenRepository.save(PasswordResetToken.builder()
                    .user(user).token(token).expiresAt(LocalDateTime.now().plusMinutes(30)).build());
            emailService.sendPasswordResetEmail(user, token);
        });
    }

    public void resendVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.isEmailVerified()) return;
            emailVerificationTokenRepository.deleteByUserId(user.getId());
            String token = UUID.randomUUID().toString();
            emailVerificationTokenRepository.save(EmailVerificationToken.builder()
                    .user(user).token(token).expiresAt(LocalDateTime.now().plusHours(24)).build());
            emailService.sendVerificationEmail(user, token);
        });
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken reset = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));
        if (reset.getExpiresAt().isBefore(LocalDateTime.now()) || reset.isUsed()) {
            throw new BadRequestException("Invalid or expired reset token");
        }
        User user = reset.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        reset.setUsed(true);
        passwordResetTokenRepository.save(reset);
    }

}
