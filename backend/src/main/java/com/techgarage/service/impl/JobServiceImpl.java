package com.techgarage.service.impl;

import com.techgarage.dto.job.*;
import com.techgarage.entity.*;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.repository.JobRepository;
import com.techgarage.repository.ProblemRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.JobService;
import com.techgarage.service.NotificationService;
import com.techgarage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final ProblemRepository problemRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

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
    public JobResponse updateStatus(Long id, JobStatusUpdateRequest request) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();

        if (!job.getFreelancer().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the assigned freelancer can update job progress");
        }
        if (job.getStatus() == JobStatus.COMPLETED || job.getStatus() == JobStatus.CANCELLED) {
            throw new BadRequestException("This job is already closed");
        }

        job.setStatus(request.getStatus());
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
        // FIX: Do not reassign 'job' – keep it effectively final for the lambda below
        jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.COMPLETED);
        problemRepository.save(problem);

        freelancerProfileRepository.findByUserId(job.getFreelancer().getId()).ifPresent(profile -> {
            profile.setTotalEarnings(profile.getTotalEarnings() + job.getAgreedPrice());
            freelancerProfileRepository.save(profile);
        });

        notificationService.notify(job.getFreelancer().getId(),
                "Job #" + job.getId() + " (\"" + problem.getTitle() + "\") was completed and payment released.");

        return toResponse(job);
    }

    @Override
    public JobResponse cancel(Long id) {
        Job job = getJobOrThrow(id);
        User me = securityUtil.getCurrentUser();
        assertParticipant(job, me);
        if (job.getStatus() == JobStatus.COMPLETED) {
            throw new BadRequestException("A completed job cannot be cancelled");
        }

        job.setStatus(JobStatus.CANCELLED);
        job = jobRepository.save(job);

        Problem problem = job.getProblem();
        problem.setStatus(ProblemStatus.CANCELLED);
        problemRepository.save(problem);

        return toResponse(job);
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