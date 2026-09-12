package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.profile.FreelancerProfileRequest;
import com.techgarage.dto.profile.FreelancerProfileResponse;
import com.techgarage.service.FreelancerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/freelancers")
@RequiredArgsConstructor
public class FreelancerController {

    private final FreelancerProfileService freelancerProfileService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FreelancerProfileResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(freelancerProfileService.getAll()));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(freelancerProfileService.getMyProfile()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(freelancerProfileService.getByUserId(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<FreelancerProfileResponse>> update(@RequestBody FreelancerProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Profile updated", freelancerProfileService.update(request)));
    }
}
