package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.profile.UserProfileRequest;
import com.techgarage.dto.profile.UserProfileResponse;
import com.techgarage.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(userProfileService.getMine()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> update(@RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Account updated", userProfileService.update(request)));
    }
}
