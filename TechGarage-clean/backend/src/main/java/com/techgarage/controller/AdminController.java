package com.techgarage.controller;

import com.techgarage.dto.admin.AdminStatsResponse;
import com.techgarage.dto.admin.AdminUserResponse;
import com.techgarage.dto.admin.DisputeResolveRequest;
import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.job.JobResponse;
import com.techgarage.dto.problem.ProblemResponse;
import com.techgarage.entity.Dispute;
import com.techgarage.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getStats()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> users() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllUsers()));
    }

    @GetMapping("/clients")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> clients() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getClients()));
    }

    @GetMapping("/freelancers")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> freelancers() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getFreelancers()));
    }

    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> problems() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllProblems()));
    }

    @GetMapping("/jobs")
    public ResponseEntity<ApiResponse<List<JobResponse>>> jobs() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllJobs()));
    }

    @GetMapping("/disputes")
    public ResponseEntity<ApiResponse<List<Dispute>>> disputes() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllDisputes()));
    }

    @PutMapping("/disputes/{id}/resolve")
    public ResponseEntity<ApiResponse<Dispute>> resolveDispute(@PathVariable Long id, @Valid @RequestBody DisputeResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Dispute resolved", adminService.resolveDispute(id, request)));
    }

    @PutMapping("/freelancers/{id}/verify")
    public ResponseEntity<ApiResponse<Object>> verifyFreelancer(@PathVariable Long id) {
        adminService.verifyFreelancer(id);
        return ResponseEntity.ok(ApiResponse.ok("Freelancer verified", null));
    }

    @PutMapping("/users/{id}/suspend")
    public ResponseEntity<ApiResponse<Object>> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User suspended", null));
    }

    @PutMapping("/users/{id}/reactivate")
    public ResponseEntity<ApiResponse<Object>> reactivateUser(@PathVariable Long id) {
        adminService.reactivateUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User reactivated", null));
    }
}
