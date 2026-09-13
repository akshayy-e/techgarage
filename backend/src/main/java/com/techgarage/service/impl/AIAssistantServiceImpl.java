package com.techgarage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgarage.dto.problem.ProblemAiSuggestionRequest;
import com.techgarage.dto.problem.ProblemAiSuggestionResponse;
import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;
import com.techgarage.service.AIAssistantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Backs the "Analyze with AI" button on the Post Problem form. Tries a real Claude call first
 * (see {@link AnthropicClient}); if no API key is configured, the call fails, or the response
 * can't be parsed, falls back to simple heuristics built on the same rule-based classifier used
 * by {@link AIClassificationServiceImpl} — so the button always returns something useful, and the
 * response tells the client which path produced it via {@code aiGenerated}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIAssistantServiceImpl implements AIAssistantService {

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ProblemAiSuggestionResponse suggest(ProblemAiSuggestionRequest request) {
        JsonNode ai = anthropicClient.isConfigured() ? tryAiSuggest(request) : null;
        if (ai != null) {
            ProblemAiSuggestionResponse fromAi = tryBuildFromAi(ai);
            if (fromAi != null) return fromAi;
        }
        return buildFallback(request);
    }

    private JsonNode tryAiSuggest(ProblemAiSuggestionRequest request) {
        String system = "You are the intake assistant for TechGarage, a marketplace where clients post " +
                "broken software for freelancers to fix. Given a draft problem title and description, " +
                "respond with ONLY a single compact JSON object — no markdown fences, no commentary — " +
                "with exactly these keys: " +
                "\"category\" (one of FRONTEND, BACKEND, DATABASE, API, CLOUD, DEVOPS, MOBILE, UI_UX, SECURITY, AI_ML, OTHER), " +
                "\"technology\" (short name of the primary technology/framework involved), " +
                "\"priority\" (one of LOW, MEDIUM, HIGH, EMERGENCY), " +
                "\"budgetMin\" and \"budgetMax\" (a fair USD budget range as plain numbers for a freelancer to fix this), " +
                "\"summary\" (one clear, well-written sentence restating the problem, fixing grammar/clarity but keeping all technical facts), " +
                "\"clarifyingQuestions\" (an array of exactly 3 short questions a freelancer would want answered before starting).";
        String user = "Title: " + safe(request.getTitle()) + "\nDescription: " + safe(request.getDescription());

        String raw = anthropicClient.complete(system, user);
        if (raw == null) return null;
        try {
            return objectMapper.readTree(stripCodeFence(raw));
        } catch (Exception e) {
            log.warn("Could not parse AI suggestion response, falling back to heuristics: {}", e.getMessage());
            return null;
        }
    }

    private ProblemAiSuggestionResponse tryBuildFromAi(JsonNode ai) {
        try {
            ProblemCategory category = parseCategory(ai.path("category").asText(null));
            Priority priority = parsePriority(ai.path("priority").asText(null));
            String technology = ai.path("technology").asText(null);
            if (category == null || priority == null || technology == null || technology.isBlank()) {
                return null; // malformed AI response — let the caller fall back
            }

            List<String> questions = new ArrayList<>();
            if (ai.path("clarifyingQuestions").isArray()) {
                List<String> finalQuestions = questions;
                ai.path("clarifyingQuestions").forEach(q -> {
                    String s = q.asText(null);
                    if (s != null && !s.isBlank()) finalQuestions.add(s);
                });
            }
            if (questions.isEmpty()) {
                questions = defaultClarifyingQuestions(category);
            }

            double budgetMin = ai.path("budgetMin").asDouble(defaultBudgetMin(priority, category));
            double budgetMax = ai.path("budgetMax").asDouble(defaultBudgetMax(priority, category));
            if (budgetMax < budgetMin) {
                double tmp = budgetMin;
                budgetMin = budgetMax;
                budgetMax = tmp;
            }

            String summary = ai.path("summary").asText(null);

            return ProblemAiSuggestionResponse.builder()
                    .category(category)
                    .technology(technology)
                    .priority(priority)
                    .suggestedBudgetMin(round(budgetMin))
                    .suggestedBudgetMax(round(budgetMax))
                    .summary((summary != null && !summary.isBlank()) ? summary : null)
                    .clarifyingQuestions(questions)
                    .aiGenerated(true)
                    .build();
        } catch (Exception e) {
            log.warn("Unexpected shape in AI suggestion response, falling back to heuristics: {}", e.getMessage());
            return null;
        }
    }

    private ProblemAiSuggestionResponse buildFallback(ProblemAiSuggestionRequest request) {
        String text = (safe(request.getTitle()) + " " + safe(request.getDescription())).toLowerCase();
        ProblemCategory category = RuleBasedProblemClassifier.detectCategory(text);
        String technology = RuleBasedProblemClassifier.detectTechnology(text);
        Priority priority = RuleBasedProblemClassifier.detectPriority(text);

        return ProblemAiSuggestionResponse.builder()
                .category(category)
                .technology(technology)
                .priority(priority)
                .suggestedBudgetMin(round(defaultBudgetMin(priority, category)))
                .suggestedBudgetMax(round(defaultBudgetMax(priority, category)))
                .summary(null)
                .clarifyingQuestions(defaultClarifyingQuestions(category))
                .aiGenerated(false)
                .build();
    }

    private double defaultBudgetMin(Priority priority, ProblemCategory category) {
        return baseBudget(priority)[0] * categoryMultiplier(category);
    }

    private double defaultBudgetMax(Priority priority, ProblemCategory category) {
        return baseBudget(priority)[1] * categoryMultiplier(category);
    }

    private double[] baseBudget(Priority priority) {
        if (priority == null) priority = Priority.MEDIUM;
        return switch (priority) {
            case EMERGENCY -> new double[]{150, 400};
            case HIGH -> new double[]{100, 300};
            case LOW -> new double[]{30, 120};
            default -> new double[]{60, 200};
        };
    }

    private double categoryMultiplier(ProblemCategory category) {
        if (category == null) return 1.0;
        return switch (category) {
            case DEVOPS, CLOUD, SECURITY, AI_ML -> 1.3;
            case DATABASE, BACKEND -> 1.1;
            default -> 1.0;
        };
    }

    private double round(double value) {
        return Math.round(value / 5.0) * 5.0; // round to nearest $5 for tidy display
    }

    private List<String> defaultClarifyingQuestions(ProblemCategory category) {
        List<String> questions = new ArrayList<>();
        questions.add("What error messages or logs are you seeing, if any?");
        questions.add("Which environment does this happen in (local, staging, production)?");
        if (category == ProblemCategory.DATABASE || category == ProblemCategory.BACKEND) {
            questions.add("Roughly how much data/traffic is involved?");
        } else if (category == ProblemCategory.UI_UX || category == ProblemCategory.FRONTEND) {
            questions.add("Which browsers/devices does this affect?");
        } else {
            questions.add("When did this last work correctly, and what changed since then?");
        }
        return questions;
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
}
