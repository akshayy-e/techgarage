package com.techgarage.service.impl;

import com.techgarage.dto.proposal.ProposalRequest;
import com.techgarage.dto.proposal.ProposalResponse;
import com.techgarage.entity.*;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.*;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.NotificationService;
import com.techgarage.service.PaymentService;
import com.techgarage.service.ProposalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProblemRepository problemRepository;
    private final JobRepository jobRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @Override
    public ProposalResponse submit(Long problemId, ProposalRequest request) {
        User freelancer = securityUtil.getCurrentUser();
        if (freelancer.getRole() != Role.FREELANCER) {
            throw new ForbiddenException("Only freelancers can submit proposals");
        }

        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        if (problem.getStatus() != ProblemStatus.OPEN && problem.getStatus() != ProblemStatus.PROPOSALS_RECEIVED) {
            throw new BadRequestException("This problem is no longer accepting proposals");
        }

        proposalRepository.findByProblemIdAndFreelancerId(problemId, freelancer.getId())
                .ifPresent(p -> { throw new BadRequestException("You have already submitted a proposal for this problem"); });

        Proposal proposal = Proposal.builder()
                .problem(problem)
                .freelancer(freelancer)
                .price(request.getPrice())
                .estimatedDays(request.getEstimatedDays())
                .message(request.getMessage())
                .status(ProposalStatus.PENDING)
                .build();

        proposal = proposalRepository.save(proposal);

        problem.setStatus(ProblemStatus.PROPOSALS_RECEIVED);
        problemRepository.save(problem);

        notificationService.notify(problem.getClient().getId(),
                "New proposal received from " + freelancer.getName() + " for \"" + problem.getTitle() + "\"");

        return toResponse(proposal);
    }

    @Override
    public List<ProposalResponse> getForProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        User me = securityUtil.getCurrentUser();
        if (!problem.getClient().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only view proposals for your own problems");
        }
        return proposalRepository.findByProblemIdOrderByCreatedAtDesc(problemId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProposalResponse> getMyProposals() {
        User freelancer = securityUtil.getCurrentUser();
        return proposalRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancer.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProposalResponse accept(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        Problem problem = proposal.getProblem();
        User me = securityUtil.getCurrentUser();
        if (!problem.getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only accept proposals for your own problems");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("This proposal has already been processed");
        }
        if (jobRepository.existsByProblemId(problem.getId())) {
            throw new BadRequestException("This problem already has an assigned job");
        }

        proposal.setStatus(ProposalStatus.ACCEPTED);
        proposalRepository.save(proposal);

        // Reject all other pending proposals for this problem
        List<Proposal> others = proposalRepository.findByProblemIdAndStatus(problem.getId(), ProposalStatus.PENDING);
        for (Proposal other : others) {
            other.setStatus(ProposalStatus.REJECTED);
            proposalRepository.save(other);
            notificationService.notify(other.getFreelancer().getId(),
                    "Your proposal for \"" + problem.getTitle() + "\" was not selected");
        }

        problem.setStatus(ProblemStatus.ASSIGNED);
        problemRepository.save(problem);

        Job job = Job.builder()
                .problem(problem)
                .client(problem.getClient())
                .freelancer(proposal.getFreelancer())
                .agreedPrice(proposal.getPrice())
                .status(JobStatus.ASSIGNED)
                .build();
        // Payment remains PENDING until the client completes the online checkout.
        // The freelancer cannot start work until a verified gateway payment moves it to HELD.
        job = jobRepository.save(job);

        notificationService.notify(proposal.getFreelancer().getId(),
                "Congratulations! Your proposal for \"" + problem.getTitle() + "\" was accepted. Job #" + job.getId() + " is awaiting client payment.");
        notificationService.notify(problem.getClient().getId(),
                "You accepted " + proposal.getFreelancer().getName() + "'s proposal. Job #" + job.getId() + " is awaiting client payment.");

        return toResponse(proposal);
    }

    @Override
    public ProposalResponse reject(Long proposalId) {
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new ResourceNotFoundException("Proposal not found"));

        User me = securityUtil.getCurrentUser();
        if (!proposal.getProblem().getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only reject proposals for your own problems");
        }
        if (proposal.getStatus() != ProposalStatus.PENDING) {
            throw new BadRequestException("This proposal has already been processed");
        }

        proposal.setStatus(ProposalStatus.REJECTED);
        proposalRepository.save(proposal);

        notificationService.notify(proposal.getFreelancer().getId(),
                "Your proposal for \"" + proposal.getProblem().getTitle() + "\" was not selected");

        return toResponse(proposal);
    }

    private ProposalResponse toResponse(Proposal p) {
        var profile = freelancerProfileRepository.findByUserId(p.getFreelancer().getId()).orElse(null);
        return ProposalResponse.builder()
                .id(p.getId())
                .problemId(p.getProblem().getId())
                .problemTitle(p.getProblem().getTitle())
                .freelancerId(p.getFreelancer().getId())
                .freelancerName(p.getFreelancer().getName())
                .freelancerRating(profile != null ? profile.getRating() : 0.0)
                .freelancerSkills(profile != null ? profile.getSkills() : "")
                .freelancerExperience(profile != null ? profile.getExperienceYears() : 0)
                .price(p.getPrice())
                .estimatedDays(p.getEstimatedDays())
                .message(p.getMessage())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
