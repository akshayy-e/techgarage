package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.profile.FreelancerProfileRequest;
import com.techgarage.dto.profile.FreelancerProfileResponse;
import com.techgarage.dto.profile.FreelancerDirectoryResponse;
import com.techgarage.service.FreelancerDirectoryService;
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
    private final FreelancerDirectoryService freelancerDirectoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FreelancerDirectoryResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean verified,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) String technology) {
        return ResponseEntity.ok(ApiResponse.ok(freelancerDirectoryService.search(query, verified, available, minRating, technology)));
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
