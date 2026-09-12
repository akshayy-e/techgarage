package com.techgarage.service.impl;

import com.techgarage.dto.admin.AdminStatsResponse;
import com.techgarage.dto.admin.AdminUserResponse;
import com.techgarage.dto.admin.DisputeRequest;
import com.techgarage.dto.admin.DisputeResolveRequest;
import com.techgarage.dto.job.JobResponse;
import com.techgarage.dto.problem.ProblemResponse;
import com.techgarage.entity.*;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.*;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.AdminService;
import com.techgarage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final ProblemRepository problemRepository;
    private final ProposalRepository proposalRepository;
    private final JobRepository jobRepository;
    private final DisputeRepository disputeRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;

    @Override
    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalFreelancers(userRepository.findByRole(Role.FREELANCER).size())
                .totalClients(userRepository.findByRole(Role.CLIENT).size())
                .totalProblems(problemRepository.count())
                .activeJobs(jobRepository.count() - jobRepository.countByStatus(JobStatus.COMPLETED) - jobRepository.countByStatus(JobStatus.CANCELLED))
                .completedJobs(jobRepository.countByStatus(JobStatus.COMPLETED))
                .totalDisputes(disputeRepository.count())
                .openDisputes(disputeRepository.countByStatus(DisputeStatus.OPEN))
                .build();
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toAdminUserResponse).collect(Collectors.toList());
    }

    @Override
    public List<AdminUserResponse> getClients() {
        return userRepository.findByRole(Role.CLIENT).stream().map(this::toAdminUserResponse).collect(Collectors.toList());
    }

    @Override
    public List<AdminUserResponse> getFreelancers() {
        return userRepository.findByRole(Role.FREELANCER).stream().map(this::toAdminUserResponse).collect(Collectors.toList());
    }

    /**
     * Maps a User to its admin-list DTO, pulling in the freelancer's verified flag
     * (from FreelancerProfile) when applicable so the Users screen reflects Verify/Suspend
     * actions instead of looking static no matter what an admin clicks.
     */
    private AdminUserResponse toAdminUserResponse(User user) {
        Boolean verified = null;
        if (user.getRole() == Role.FREELANCER) {
            verified = freelancerProfileRepository.findByUserId(user.getId())
                    .map(FreelancerProfile::getVerified)
                    .orElse(false);
        }
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .phone(user.getPhone())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .verified(verified)
                .build();
    }

    @Override
    public List<ProblemResponse> getAllProblems() {
        return problemRepository.findAllByOrderByCreatedAtDesc().stream().map(p ->
                ProblemResponse.builder()
                        .id(p.getId())
                        .clientId(p.getClient().getId())
                        .clientName(p.getClient().getName())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .category(p.getCategory())
                        .technology(p.getTechnology())
                        .priority(p.getPriority())
                        .budget(p.getBudget())
                        .expectedCompletionDate(p.getExpectedCompletionDate())
                        .status(p.getStatus())
                        .attachmentUrl(p.getAttachmentUrl())
                        .proposalCount((long) proposalRepository.findByProblemIdOrderByCreatedAtDesc(p.getId()).size())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public List<JobResponse> getAllJobs() {
        return jobRepository.findAllByOrderByCreatedAtDesc().stream().map(j ->
                JobResponse.builder()
                        .id(j.getId())
                        .problemId(j.getProblem().getId())
                        .problemTitle(j.getProblem().getTitle())
                        .clientId(j.getClient().getId())
                        .clientName(j.getClient().getName())
                        .freelancerId(j.getFreelancer().getId())
                        .freelancerName(j.getFreelancer().getName())
                        .agreedPrice(j.getAgreedPrice())
                        .status(j.getStatus())
                        .paymentStatus(j.getPaymentStatus())
                        .solutionNotes(j.getSolutionNotes())
                        .revisionNotes(j.getRevisionNotes())
                        .startedAt(j.getStartedAt())
                        .completedAt(j.getCompletedAt())
                        .createdAt(j.getCreatedAt())
                        .build()
        ).collect(Collectors.toList());
    }

    @Override
    public void verifyFreelancer(Long userId) {
        FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
        profile.setVerified(true);
        freelancerProfileRepository.save(profile);
        notificationService.notify(userId, "Your freelancer profile has been verified by TechGarage admin.");
    }

    @Override
    public void suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    public void reactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Override
    public Dispute raiseDispute(Long jobId, DisputeRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User me = securityUtil.getCurrentUser();

        boolean participant = job.getClient().getId().equals(me.getId()) || job.getFreelancer().getId().equals(me.getId());
        if (!participant) {
            throw new ForbiddenException("You are not a participant in this job");
        }

        Dispute dispute = Dispute.builder()
                .job(job)
                .raisedBy(me)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .build();
        dispute = disputeRepository.save(dispute);

        job.setStatus(JobStatus.DISPUTED);
        jobRepository.save(job);
        job.getProblem().setStatus(ProblemStatus.DISPUTED);
        problemRepository.save(job.getProblem());

        return dispute;
    }

    @Override
    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Dispute resolveDispute(Long disputeId, DisputeResolveRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));
        dispute.setStatus(request.getStatus());
        dispute.setAdminResponse(request.getAdminResponse());
        dispute = disputeRepository.save(dispute);

        notificationService.notify(dispute.getRaisedBy().getId(),
                "Your dispute on Job #" + dispute.getJob().getId() + " was updated: " + request.getStatus());

        return dispute;
    }
}
