package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.review.ReviewRequest;
import com.techgarage.dto.review.ReviewResponse;
import com.techgarage.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/api/jobs/{jobId}/review")
    public ResponseEntity<ApiResponse<ReviewResponse>> submit(@PathVariable Long jobId, @Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Review submitted", reviewService.submit(jobId, request)));
    }

    @GetMapping("/api/freelancers/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getForFreelancer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getForFreelancer(id)));
    }
}
