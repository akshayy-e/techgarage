package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.job.*;
import com.techgarage.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<JobResponse>>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(jobService.getMyJobs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<JobResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(jobService.getById(id)));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<JobResponse>> updateStatus(@PathVariable Long id, @Valid @RequestBody JobStatusUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Job status updated", jobService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/submit-solution")
    public ResponseEntity<ApiResponse<JobResponse>> submitSolution(@PathVariable Long id, @Valid @RequestBody SolutionSubmitRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Solution submitted", jobService.submitSolution(id, request)));
    }

    @PostMapping("/{id}/revision")
    public ResponseEntity<ApiResponse<JobResponse>> requestRevision(@PathVariable Long id, @Valid @RequestBody RevisionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Revision requested", jobService.requestRevision(id, request)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<JobResponse>> complete(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Job completed, payment released", jobService.complete(id)));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<JobResponse>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Job cancelled", jobService.cancel(id)));
    }

    @GetMapping("/{id}/change-requests")
    public ResponseEntity<ApiResponse<List<ChangeRequestResponse>>> getChangeRequests(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(jobService.getChangeRequests(id)));
    }

    @PostMapping("/{id}/change-requests")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> createChangeRequest(@PathVariable Long id, @Valid @RequestBody ChangeRequestCreate request) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Change request created", jobService.createChangeRequest(id, request)));
    }

    @PutMapping("/change-requests/{changeRequestId}/respond")
    public ResponseEntity<ApiResponse<ChangeRequestResponse>> respondToChangeRequest(@PathVariable Long changeRequestId, @RequestParam boolean accept) {
        return ResponseEntity.ok(ApiResponse.ok("Change request updated", jobService.respondToChangeRequest(changeRequestId, accept)));
    }
}
