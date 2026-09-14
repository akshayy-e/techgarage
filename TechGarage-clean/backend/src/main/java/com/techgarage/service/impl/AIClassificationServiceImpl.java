package com.techgarage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgarage.dto.problem.ProblemRequest;
import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;
import com.techgarage.service.AIClassificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Issue classifier used when a client leaves category / technology / priority blank on a new
 * problem. Tries a real Claude call first (when {@code ANTHROPIC_API_KEY} is configured, see
 * {@link AnthropicClient}) and transparently falls back to the original rule-based keyword
 * classifier when no key is set, the call fails, or the model's response can't be parsed — so
 * problem posting always works, with or without an API key, and a flaky AI call never blocks a
 * client from posting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIClassificationServiceImpl implements AIClassificationService {

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProblemRequest classify(ProblemRequest request) {
        boolean needsCategory = request.getCategory() == null;
        boolean needsTechnology = request.getTechnology() == null || request.getTechnology().isBlank();
        boolean needsPriority = request.getPriority() == null;

        if (!needsCategory && !needsTechnology && !needsPriority) {
            return request; // client specified everything explicitly — nothing to classify
        }

        JsonNode ai = anthropicClient.isConfigured() ? tryAiClassify(request) : null;
        String text = textOf(request);

        if (needsCategory) {
            ProblemCategory category = ai != null ? parseCategory(ai.path("category").asText(null)) : null;
            request.setCategory(category != null ? category : RuleBasedProblemClassifier.detectCategory(text));
        }
        if (needsTechnology) {
            String technology = ai != null ? ai.path("technology").asText(null) : null;
            request.setTechnology((technology != null && !technology.isBlank()) ? technology : RuleBasedProblemClassifier.detectTechnology(text));
        }
        if (needsPriority) {
            Priority priority = ai != null ? parsePriority(ai.path("priority").asText(null)) : null;
            request.setPriority(priority != null ? priority : RuleBasedProblemClassifier.detectPriority(text));
        }
        return request;
    }

    private JsonNode tryAiClassify(ProblemRequest request) {
        String system = "You triage incoming software problem reports for a freelance tech-repair " +
                "marketplace called TechGarage. Given a problem title and description, respond with " +
                "ONLY a single compact JSON object — no markdown fences, no commentary — with exactly " +
                "these keys: " +
                "\"category\" (one of FRONTEND, BACKEND, DATABASE, API, CLOUD, DEVOPS, MOBILE, UI_UX, " +
                "SECURITY, AI_ML, OTHER), " +
                "\"technology\" (a short human-readable name for the primary technology/framework " +
                "involved, e.g. \"React\" or \"Java / Spring Boot\"), " +
                "\"priority\" (one of LOW, MEDIUM, HIGH, EMERGENCY, based on how urgent/business-critical " +
                "the report sounds).";
        String user = "Title: " + safe(request.getTitle()) + "\nDescription: " + safe(request.getDescription());

        String raw = anthropicClient.complete(system, user);
        if (raw == null) return null;
        try {
            return objectMapper.readTree(stripCodeFence(raw));
        } catch (Exception e) {
            log.warn("Could not parse AI classification response, falling back to rules: {}", e.getMessage());
            return null;
        }
    }

    private String stripCodeFence(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\s*", "").replaceFirst("```\\s*$", "");
        }
        return trimmed.trim();
    }

    private ProblemCategory parseCategory(String value) {
        if (value == null) return null;
        try {
            return ProblemCategory.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Priority parsePriority(String value) {
        if (value == null) return null;
        try {
            return Priority.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String textOf(ProblemRequest request) {
        return (safe(request.getTitle()) + " " + safe(request.getDescription())).toLowerCase();
    }
}
