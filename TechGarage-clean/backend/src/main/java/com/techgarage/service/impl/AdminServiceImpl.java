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
import com.techgarage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private final PaymentService paymentService;

    @Override
    public AdminStatsResponse getStats() {
        return AdminStatsResponse.builder()
                .totalUsers(userRepository.count())
                .totalFreelancers(userRepository.countByRole(Role.FREELANCER))
                .totalClients(userRepository.countByRole(Role.CLIENT))
                .totalProblems(problemRepository.count())
                .activeJobs(jobRepository.count() - jobRepository.countByStatus(JobStatus.COMPLETED) - jobRepository.countByStatus(JobStatus.CANCELLED))
                .completedJobs(jobRepository.countByStatus(JobStatus.COMPLETED))
                .totalDisputes(disputeRepository.count())
                .openDisputes(disputeRepository.countByStatus(DisputeStatus.OPEN))
                .build();
    }

    @Override
    public List<AdminUserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return toAdminUserResponses(users);
    }

    @Override
    public List<AdminUserResponse> getClients() {
        return toAdminUserResponses(userRepository.findByRole(Role.CLIENT));
    }

    @Override
    public List<AdminUserResponse> getFreelancers() {
        return toAdminUserResponses(userRepository.findByRole(Role.FREELANCER));
    }

    private List<AdminUserResponse> toAdminUserResponses(List<User> users) {
        Map<Long, FreelancerProfile> profiles = freelancerProfileRepository.findAll().stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));
        return users.stream().map(user -> toAdminUserResponse(user, profiles)).collect(Collectors.toList());
    }

    /**
     * Maps a User to its admin-list DTO, pulling in the freelancer's verified flag
     * (from FreelancerProfile) when applicable so the Users screen reflects Verify/Suspend
     * actions instead of looking static no matter what an admin clicks.
     */
    private AdminUserResponse toAdminUserResponse(User user, Map<Long, FreelancerProfile> profiles) {
        Boolean verified = null;
        if (user.getRole() == Role.FREELANCER) {
            FreelancerProfile profile = profiles.get(user.getId());
            verified = profile != null && Boolean.TRUE.equals(profile.getVerified());
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
                .emailVerified(user.isEmailVerified())
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
                        .proposalCount((long) proposalRepository.countByProblemId(p.getId()))
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
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() != Role.FREELANCER) throw new com.techgarage.exception.BadRequestException("Only freelancer accounts can be verified");
        if (!user.isEmailVerified()) throw new com.techgarage.exception.BadRequestException("Freelancer must verify their email before admin verification");
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
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new com.techgarage.exception.BadRequestException("A closed job cannot be disputed");
        }
        if (disputeRepository.existsByJobIdAndStatus(jobId, DisputeStatus.OPEN)) {
            throw new com.techgarage.exception.BadRequestException("This job already has an open dispute");
        }

        Dispute dispute = Dispute.builder()
                .job(job)
                .raisedBy(me)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(DisputeStatus.OPEN)
                .previousJobStatus(job.getStatus())
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
    @Transactional
    public Dispute resolveDispute(Long disputeId, DisputeResolveRequest request) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispute not found"));
        if (dispute.getStatus() == DisputeStatus.RESOLVED || dispute.getStatus() == DisputeStatus.REJECTED) {
            throw new com.techgarage.exception.BadRequestException("This dispute has already been closed");
        }
        if (request.getStatus() == DisputeStatus.UNDER_REVIEW && request.getAction() != null) {
            throw new com.techgarage.exception.BadRequestException("Resolution action is only used when closing a dispute");
        }
        if ((request.getStatus() == DisputeStatus.RESOLVED || request.getStatus() == DisputeStatus.REJECTED) && request.getAction() == null) {
            throw new com.techgarage.exception.BadRequestException("Choose a resolution action");
        }
        dispute.setStatus(request.getStatus());
        dispute.setAdminResponse(request.getAdminResponse());
        if (request.getStatus() == DisputeStatus.RESOLVED || request.getStatus() == DisputeStatus.REJECTED) {
            Job job = dispute.getJob();
            var action = request.getStatus() == DisputeStatus.REJECTED ? DisputeResolutionAction.RESUME : request.getAction();
            dispute.setResolutionAction(action);
            dispute.setResolvedAt(java.time.LocalDateTime.now());
            if (action == DisputeResolutionAction.RESUME) {
                JobStatus restore = dispute.getPreviousJobStatus() == null ? JobStatus.IN_PROGRESS : dispute.getPreviousJobStatus();
                if (restore == JobStatus.DISPUTED || restore == JobStatus.COMPLETED || restore == JobStatus.CANCELLED) restore = JobStatus.IN_PROGRESS;
                job.setStatus(restore);
                job.getProblem().setStatus(restore == JobStatus.IN_PROGRESS ? ProblemStatus.IN_PROGRESS : ProblemStatus.ASSIGNED);
                jobRepository.save(job); problemRepository.save(job.getProblem());
            } else if (action == DisputeResolutionAction.REFUND_AND_CANCEL) {
                if (job.getPaymentStatus() == PaymentStatus.HELD) {
                    paymentService.refundPayment(job);
                }
                job.setStatus(JobStatus.CANCELLED); job.getProblem().setStatus(ProblemStatus.CANCELLED);
                jobRepository.save(job); problemRepository.save(job.getProblem());
            } else if (action == DisputeResolutionAction.RELEASE_AND_COMPLETE) {
                if (job.getStatus() != JobStatus.COMPLETED) {
                    job.setStatus(JobStatus.COMPLETED); job.setCompletedAt(java.time.LocalDateTime.now());
                    paymentService.releasePayment(job);
                    job.getProblem().setStatus(ProblemStatus.COMPLETED);
                    freelancerProfileRepository.findByUserId(job.getFreelancer().getId()).ifPresent(profile -> {
                        profile.setTotalEarnings(profile.getTotalEarnings() + paymentService.getFreelancerNetAmount(job));
                        freelancerProfileRepository.save(profile);
                    });
                    jobRepository.save(job); problemRepository.save(job.getProblem());
                }
            }
        }
        dispute = disputeRepository.save(dispute);
        notificationService.notify(dispute.getJob().getClient().getId(), "Dispute on Job #" + dispute.getJob().getId() + " was updated: " + request.getStatus());
        notificationService.notify(dispute.getJob().getFreelancer().getId(), "Dispute on Job #" + dispute.getJob().getId() + " was updated: " + request.getStatus());
        return dispute;
    }
}
