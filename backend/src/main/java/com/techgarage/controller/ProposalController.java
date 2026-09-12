package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.proposal.ProposalRequest;
import com.techgarage.dto.proposal.ProposalResponse;
import com.techgarage.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @PostMapping("/api/problems/{problemId}/proposals")
    public ResponseEntity<ApiResponse<ProposalResponse>> submit(@PathVariable Long problemId, @Valid @RequestBody ProposalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Proposal submitted", proposalService.submit(problemId, request)));
    }

    @GetMapping("/api/problems/{problemId}/proposals")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getForProblem(@PathVariable Long problemId) {
        return ResponseEntity.ok(ApiResponse.ok(proposalService.getForProblem(problemId)));
    }

    @GetMapping("/api/proposals/mine")
    public ResponseEntity<ApiResponse<List<ProposalResponse>>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(proposalService.getMyProposals()));
    }

    @PutMapping("/api/proposals/{id}/accept")
    public ResponseEntity<ApiResponse<ProposalResponse>> accept(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal accepted, job created", proposalService.accept(id)));
    }

    @PutMapping("/api/proposals/{id}/reject")
    public ResponseEntity<ApiResponse<ProposalResponse>> reject(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Proposal rejected", proposalService.reject(id)));
    }
}
