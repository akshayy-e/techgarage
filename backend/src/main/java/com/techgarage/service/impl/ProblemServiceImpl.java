package com.techgarage.service.impl;

import com.techgarage.dto.problem.ProblemAiSuggestionRequest;
import com.techgarage.dto.problem.ProblemAiSuggestionResponse;
import com.techgarage.dto.problem.ProblemRequest;
import com.techgarage.dto.problem.ProblemResponse;
import com.techgarage.entity.*;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.ProblemRepository;
import com.techgarage.repository.ProposalRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.AIAssistantService;
import com.techgarage.service.AIClassificationService;
import com.techgarage.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {

    private final ProblemRepository problemRepository;
    private final ProposalRepository proposalRepository;
    private final SecurityUtil securityUtil;
    private final AIClassificationService aiClassificationService;
    private final AIAssistantService aiAssistantService;

    @Override
    public ProblemResponse create(ProblemRequest request) {
        User client = securityUtil.getCurrentUser();
        if (client.getRole() != Role.CLIENT) {
            throw new ForbiddenException("Only clients can post problems");
        }

        request = aiClassificationService.classify(request);

        Problem problem = Problem.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .technology(request.getTechnology())
                .priority(request.getPriority() == null ? Priority.MEDIUM : request.getPriority())
                .budget(request.getBudget())
                .expectedCompletionDate(request.getExpectedCompletionDate())
                .attachmentUrl(request.getAttachmentUrl())
                .status(ProblemStatus.OPEN)
                .build();

        problem = problemRepository.save(problem);
        return toResponse(problem);
    }

    @Override
    public ProblemAiSuggestionResponse getAiSuggestion(ProblemAiSuggestionRequest request) {
        User client = securityUtil.getCurrentUser();
        if (client.getRole() != Role.CLIENT) {
            throw new ForbiddenException("Only clients can use the problem-posting AI assistant");
        }
        return aiAssistantService.suggest(request);
    }

    @Override
    public ProblemResponse getById(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        return toResponse(problem);
    }

    @Override
    public List<ProblemResponse> getAllOpen() {
        return problemRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(p -> p.getStatus() == ProblemStatus.OPEN || p.getStatus() == ProblemStatus.PROPOSALS_RECEIVED)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProblemResponse> getMyProblems() {
        User client = securityUtil.getCurrentUser();
        return problemRepository.findByClientIdOrderByCreatedAtDesc(client.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProblemResponse update(Long id, ProblemRequest request) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        User me = securityUtil.getCurrentUser();
        if (!problem.getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("You can only edit your own problems");
        }
        if (problem.getStatus() != ProblemStatus.OPEN) {
            throw new ForbiddenException("Cannot edit a problem once proposals are in progress");
        }

        problem.setTitle(request.getTitle());
        problem.setDescription(request.getDescription());
        if (request.getCategory() != null) problem.setCategory(request.getCategory());
        if (request.getTechnology() != null) problem.setTechnology(request.getTechnology());
        if (request.getPriority() != null) problem.setPriority(request.getPriority());
        if (request.getBudget() != null) problem.setBudget(request.getBudget());
        if (request.getExpectedCompletionDate() != null) problem.setExpectedCompletionDate(request.getExpectedCompletionDate());
        if (request.getAttachmentUrl() != null) problem.setAttachmentUrl(request.getAttachmentUrl());

        problem = problemRepository.save(problem);
        return toResponse(problem);
    }

    @Override
    public void delete(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
        User me = securityUtil.getCurrentUser();
        if (!problem.getClient().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You can only delete your own problems");
        }
        problemRepository.delete(problem);
    }

    private ProblemResponse toResponse(Problem p) {
        long proposalCount = proposalRepository.findByProblemIdOrderByCreatedAtDesc(p.getId()).size();
        return ProblemResponse.builder()
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
                .proposalCount(proposalCount)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
