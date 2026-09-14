package com.techgarage.service.impl;

import com.techgarage.dto.profile.UserProfileRequest;
import com.techgarage.dto.profile.UserProfileResponse;
import com.techgarage.entity.User;
import com.techgarage.repository.UserRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Override
    public UserProfileResponse getMine() {
        return toResponse(securityUtil.getCurrentUser());
    }

    @Override
    public UserProfileResponse update(UserProfileRequest request) {
        User me = securityUtil.getCurrentUser();
        if (request.getName() != null && !request.getName().isBlank()) {
            me.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            me.setPhone(request.getPhone().trim());
        }
        return toResponse(userRepository.save(me));
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
