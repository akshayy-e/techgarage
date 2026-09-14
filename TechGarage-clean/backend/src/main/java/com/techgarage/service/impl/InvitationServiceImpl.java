package com.techgarage.service.impl;

import com.techgarage.dto.invitation.InvitationRequest;
import com.techgarage.dto.invitation.InvitationResponse;
import com.techgarage.entity.*;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.FreelancerInvitationRepository;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.repository.ProblemRepository;
import com.techgarage.repository.UserRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.InvitationService;
import com.techgarage.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements InvitationService {
    private final FreelancerInvitationRepository invitationRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;
    private final FreelancerProfileRepository profileRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InvitationResponse invite(Long freelancerId, InvitationRequest request) {
        User client = securityUtil.getCurrentUser();
        if (client.getRole() != Role.CLIENT) throw new ForbiddenException("Only clients can invite freelancers");

        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        if (!problem.getClient().getId().equals(client.getId())) throw new ForbiddenException("You can only invite freelancers to your own problems");
        if (!(problem.getStatus() == ProblemStatus.OPEN || problem.getStatus() == ProblemStatus.PROPOSALS_RECEIVED)) {
            throw new BadRequestException("Invitations are only allowed while the problem is open for proposals");
        }

        User freelancer = userRepository.findById(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer not found"));
        if (freelancer.getRole() != Role.FREELANCER) throw new BadRequestException("Selected user is not a freelancer");
        FreelancerProfile profile = profileRepository.findByUserId(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
        if (!Boolean.TRUE.equals(profile.getAvailability())) throw new BadRequestException("This freelancer is currently unavailable");

        FreelancerInvitation invitation = invitationRepository.findByProblemIdAndFreelancerId(problem.getId(), freelancerId).orElse(null);
        if (invitation != null) {
            if (invitation.getStatus() == InvitationStatus.PENDING) throw new BadRequestException("An invitation is already pending");
            if (invitation.getStatus() == InvitationStatus.ACCEPTED) throw new BadRequestException("This freelancer already accepted the invitation");
            invitation.setStatus(InvitationStatus.PENDING);
            invitation.setRespondedAt(null);
            invitation.setMessage(request.getMessage());
        } else {
            invitation = FreelancerInvitation.builder().problem(problem).client(client).freelancer(freelancer)
                    .message(request.getMessage()).status(InvitationStatus.PENDING).build();
        }
        invitation = invitationRepository.save(invitation);
        notificationService.notify(freelancerId, client.getName() + " invited you to review problem #" + problem.getId() + ".");
        return toResponse(invitation);
    }

    @Override
    public List<InvitationResponse> getMyInvitations() {
        User me = securityUtil.getCurrentUser();
        return invitationRepository.findByFreelancerIdOrderByCreatedAtDesc(me.getId()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvitationResponse> getSentInvitations() {
        User me = securityUtil.getCurrentUser();
        if (me.getRole() != Role.CLIENT) throw new ForbiddenException("Only clients can view sent invitations");
        return invitationRepository.findByClientIdOrderByCreatedAtDesc(me.getId()).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvitationResponse respond(Long id, boolean accept) {
        User me = securityUtil.getCurrentUser();
        if (me.getRole() != Role.FREELANCER) throw new ForbiddenException("Only freelancers can respond to invitations");
        FreelancerInvitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found"));
        if (!invitation.getFreelancer().getId().equals(me.getId())) throw new ResourceNotFoundException("Invitation not found");
        if (invitation.getStatus() != InvitationStatus.PENDING) throw new BadRequestException("This invitation is no longer pending");
        if (invitation.getProblem().getStatus() != ProblemStatus.OPEN && invitation.getProblem().getStatus() != ProblemStatus.PROPOSALS_RECEIVED) {
            invitation.setStatus(InvitationStatus.EXPIRED);
            invitation.setRespondedAt(LocalDateTime.now());
            invitationRepository.save(invitation);
            throw new BadRequestException("This problem is no longer accepting proposals");
        }
        invitation.setStatus(accept ? InvitationStatus.ACCEPTED : InvitationStatus.DECLINED);
        invitation.setRespondedAt(LocalDateTime.now());
        invitationRepository.save(invitation);
        notificationService.notify(invitation.getClient().getId(), me.getName() + (accept ? " accepted" : " declined") + " your invitation for problem #" + invitation.getProblem().getId() + ".");
        return toResponse(invitation);
    }

    private InvitationResponse toResponse(FreelancerInvitation i) {
        return InvitationResponse.builder().id(i.getId()).problemId(i.getProblem().getId()).problemTitle(i.getProblem().getTitle())
                .clientId(i.getClient().getId()).clientName(i.getClient().getName()).freelancerId(i.getFreelancer().getId())
                .freelancerName(i.getFreelancer().getName()).status(i.getStatus()).message(i.getMessage()).createdAt(i.getCreatedAt())
                .respondedAt(i.getRespondedAt()).build();
    }
}
