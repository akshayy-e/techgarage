package com.techgarage.service;

import com.techgarage.dto.admin.AdminStatsResponse;
import com.techgarage.dto.admin.AdminUserResponse;
import com.techgarage.dto.admin.DisputeRequest;
import com.techgarage.dto.admin.DisputeResolveRequest;
import com.techgarage.dto.job.JobResponse;
import com.techgarage.dto.problem.ProblemResponse;
import com.techgarage.entity.Dispute;
import java.util.List;

public interface AdminService {
    AdminStatsResponse getStats();
    List<AdminUserResponse> getAllUsers();
    List<AdminUserResponse> getClients();
    List<AdminUserResponse> getFreelancers();
    List<ProblemResponse> getAllProblems();
    List<JobResponse> getAllJobs();
    void verifyFreelancer(Long userId);
    void suspendUser(Long userId);
    void reactivateUser(Long userId);
    Dispute raiseDispute(Long jobId, DisputeRequest request);
    List<Dispute> getAllDisputes();
    Dispute resolveDispute(Long disputeId, DisputeResolveRequest request);
}
