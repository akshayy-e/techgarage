package com.techgarage.controller;

import com.techgarage.dto.admin.DisputeRequest;
import com.techgarage.dto.common.ApiResponse;
import com.techgarage.entity.Dispute;
import com.techgarage.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jobs/{jobId}/disputes")
@RequiredArgsConstructor
public class DisputeController {

    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<ApiResponse<Dispute>> raise(@PathVariable Long jobId, @Valid @RequestBody DisputeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Dispute raised, admin will review", adminService.raiseDispute(jobId, request)));
    }
}
