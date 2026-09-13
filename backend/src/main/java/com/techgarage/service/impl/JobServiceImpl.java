package com.techgarage.service.impl;

import com.techgarage.dto.job.*;
import com.techgarage.entity.*;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.repository.JobRepository;
import com.techgarage.repository.ChangeRequestRepository;
import com.techgarage.repository.ProblemRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.JobService;
import com.techgarage.service.NotificationService;
import com.techgarage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.techgarage.exception.DuplicateResourceException;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ProblemRepository problemRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final ChangeRequestRepository changeRequestRepository;

    @Override
    public List<JobResponse> getMyJobs() {
        User me = securityUtil.getCurrentUser();
        List<Job> jobs = me.getRole() == Role.CLIENT
                ? jobRepository.findByClientIdOrderByCreatedAtDesc(me.getId())
                : me.getRole() == Role.FREELANCER
                ? jobRepository.findByFreelancerIdOrderByCreatedAtDesc(me.getId())
                : jobRepository.findAllByOrderByCreatedAtDesc();
        return jobs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public JobResponse getById(Long id) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        assertParticipant(job, me);
        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse updateStatus(Long id, JobStatusUpdateRequest request) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();

        if (!job.getFreelancer().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the assigned freelancer can update job progress");
        }
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED || job.getStatus() == JobStatus.DISPUTED) {
            throw new BadRequestException("This job is already closed or under dispute");
        }
        if (request.getStatus() == JobStatus.IN_PROGRESS && job.getPaymentStatus() != PaymentStatus.HELD) {
            throw new BadRequestException("Client payment must be successfully verified before work can start");
        }
        if (!isValidFreelancerTransition(job.getStatus(), request.getStatus())) {
            throw new BadRequestException("Invalid job status transition from " + job.getStatus() + " to " + request.getStatus());
        }

        job.setStatus(request.getStatus());
        if (request.getStatus() == JobStatus.IN_PROGRESS && job.getStartedAt() == null) {
            job.setStartedAt(LocalDateTime.now());
        }
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        if (request.getStatus() == JobStatus.IN_PROGRESS) {
            problem.setStatus(ProblemStatus.IN_PROGRESS);
            problemRepository.save(problem);
        }

        notificationService.notify(job.getClient().getId(),
                "Job #" + job.getId() + " status updated to " + request.getStatus());

        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse submitSolution(Long id, SolutionSubmitRequest request) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        if (!job.getFreelancer().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the assigned freelancer can submit a solution");
        }

        job.setSolutionNotes(request.getSolutionNotes());
        job.setStatus(JobStatus.SUBMITTED);
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.SUBMITTED);
        problemRepository.save(problem);

        notificationService.notify(job.getClient().getId(),
                job.getFreelancer().getName() + " submitted a solution for \"" + problem.getTitle() + "\". Please review it.");

        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse requestRevision(Long id, RevisionRequest request) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        if (!job.getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the client can request a revision");
        }
        if (job.getStatus() != JobStatus.SUBMITTED) {
            throw new BadRequestException("Revisions can only be requested after a solution is submitted");
        }

        job.setRevisionNotes(request.getRevisionNotes());
        job.setStatus(JobStatus.REVISION_REQUESTED);
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.REVISION_REQUESTED);
        problemRepository.save(problem);

        notificationService.notify(job.getFreelancer().getId(),
                job.getClient().getName() + " requested a revision on \"" + problem.getTitle() + "\"");

        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse complete(Long id) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        if (!job.getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the client can mark a job as completed");
        }
        if (job.getStatus() != JobStatus.SUBMITTED) {
            throw new BadRequestException("Only a submitted solution can be approved and completed");
        }

        job.setStatus(JobStatus.COMPLETED);
        job.setCompletedAt(LocalDateTime.now());
        paymentService.releasePayment(job);
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.COMPLETED);
        problemRepository.save(problem);

        Job finalJob = job;
        freelancerProfileRepository.findByUserId(job.getFreelancer().getId()).ifPresent(profile -> {
            profile.setTotalEarnings(profile.getTotalEarnings() + paymentService.getFreelancerNetAmount(finalJob));
            freelancerProfileRepository.save(profile);
        });

        notificationService.notify(job.getFreelancer().getId(),
                "Job #" + job.getId() + " (\"" + problem.getTitle() + "\") was completed and payment released.");

        return toResponse(job);
    }

    @Override
    @Transactional
    public JobResponse cancel(Long id) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        assertParticipant(job, me);
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new BadRequestException("This job cannot be cancelled");
        }
        if (job.getStatus() == JobStatus.DISPUTED) {
            throw new BadRequestException("A disputed job must be resolved by an admin");
        }

        job.setStatus(JobStatus.CANCELLED);
        if (job.getPaymentStatus() == PaymentStatus.HELD) {
            paymentService.refundPayment(job);
        }
        notificationService.notify(job.getFreelancer().getId(), "Job #" + job.getId() + " was cancelled. Payment status: " + job.getPaymentStatus());
        notificationService.notify(job.getClient().getId(), "Job #" + job.getId() + " was cancelled. Payment status: " + job.getPaymentStatus());
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.CANCELLED);
        problemRepository.save(problem);

        return toResponse(job);
    }

    @Override
    public List<ChangeRequestResponse> getChangeRequests(Long jobId) {
        Job job = getJobOrThrow(jobId);
        assertParticipant(job, securityUtil.getCurrentUser());
        return changeRequestRepository.findByJobIdOrderByCreatedAtDesc(jobId).stream().map(this::changeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChangeRequestResponse createChangeRequest(Long jobId, ChangeRequestCreate request) {
        Job job = getJobOrThrow(jobId);
        User me = securityUtil.getCurrentUser();
        if (!job.getClient().getId().equals(me.getId()) && !job.getFreelancer().getId().equals(me.getId())) throw new ForbiddenException("You are not a participant in this job");
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED || job.getStatus() == JobStatus.DISPUTED) throw new BadRequestException("Change requests are not allowed for this job");
        if (changeRequestRepository.existsByJobIdAndStatus(jobId, ChangeRequestStatus.PENDING)) throw new DuplicateResourceException("Resolve the existing change request before creating another");
        if (request.getAdditionalPrice() <= 0 && request.getAdditionalDays() <= 0) throw new BadRequestException("A change request must add price or time");
        ChangeRequest cr = changeRequestRepository.save(ChangeRequest.builder().job(job).requestedBy(me).description(request.getDescription()).additionalPrice(request.getAdditionalPrice()).additionalDays(request.getAdditionalDays()).build());
        User recipient = me.getId().equals(job.getClient().getId()) ? job.getFreelancer() : job.getClient();
        notificationService.notify(recipient.getId(), me.getName() + " proposed a change for Job #" + job.getId() + ".");
        return changeResponse(cr);
    }

    @Override
    @Transactional
    public ChangeRequestResponse respondToChangeRequest(Long id, boolean accept) {
        ChangeRequest cr = changeRequestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Change request not found"));
        Job job = cr.getJob(); User me = securityUtil.getCurrentUser(); assertParticipant(job, me);
        if (!job.getClient().getId().equals(me.getId()) && !job.getFreelancer().getId().equals(me.getId())) throw new ForbiddenException("You are not a participant in this job");
        if (!cr.getStatus().equals(ChangeRequestStatus.PENDING)) throw new BadRequestException("This change request has already been answered");
        if (cr.getRequestedBy().getId().equals(me.getId())) throw new BadRequestException("The requester cannot approve their own change request");
        cr.setStatus(accept ? ChangeRequestStatus.ACCEPTED : ChangeRequestStatus.REJECTED); cr.setRespondedAt(LocalDateTime.now());
        if (accept) {
            if (cr.getAdditionalPrice() > 0 && job.getPaymentStatus() == PaymentStatus.RELEASED) {
                throw new BadRequestException("A completed payment cannot be increased");
            }
            job.setAgreedPrice(job.getAgreedPrice() + cr.getAdditionalPrice());
            if (job.getPaymentStatus() == PaymentStatus.HELD && cr.getAdditionalPrice() > 0) {
                job.setPaymentStatus(PaymentStatus.PENDING);
            }
            if (job.getProblem().getExpectedCompletionDate() != null && cr.getAdditionalDays() > 0) job.getProblem().setExpectedCompletionDate(job.getProblem().getExpectedCompletionDate().plusDays(cr.getAdditionalDays()));
            jobRepository.save(job); problemRepository.save(job.getProblem());
        }
        changeRequestRepository.save(cr);
        notificationService.notify(cr.getRequestedBy().getId(), "Your change request for Job #" + job.getId() + " was " + cr.getStatus().name().toLowerCase() + ".");
        return changeResponse(cr);
    }

    private ChangeRequestResponse changeResponse(ChangeRequest c) {
        return ChangeRequestResponse.builder().id(c.getId()).jobId(c.getJob().getId()).requestedById(c.getRequestedBy().getId()).requestedByName(c.getRequestedBy().getName()).description(c.getDescription()).additionalPrice(c.getAdditionalPrice()).additionalDays(c.getAdditionalDays()).status(c.getStatus()).createdAt(c.getCreatedAt()).respondedAt(c.getRespondedAt()).build();
    }

    private boolean isValidFreelancerTransition(JobStatus current, JobStatus next) {
        return (current == JobStatus.ASSIGNED && next == JobStatus.IN_PROGRESS)
                || (current == JobStatus.IN_PROGRESS && next == JobStatus.IN_PROGRESS)
                || (current == JobStatus.REVISION_REQUESTED && next == JobStatus.REVISION_REQUESTED);
    }

    private Job getJobOrThrow(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private void assertParticipant(Job job, User user) {
        boolean isParticipant = job.getClient().getId().equals(user.getId())
                || job.getFreelancer().getId().equals(user.getId())
                || user.getRole() == Role.ADMIN;
        if (!isParticipant) {
            throw new ForbiddenException("You are not a participant in this job");
        }
    }

    private JobResponse toResponse(Job j) {
        return JobResponse.builder()
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
                .build();
    }
}
