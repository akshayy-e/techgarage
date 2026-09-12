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

    @Override
    public AuthResponse register(RegisterRequest request) {
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
                .build();

        user = userRepository.save(user);

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
}
