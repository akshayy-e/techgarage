package com.techgarage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Thin wrapper around Anthropic's Messages API (https://api.anthropic.com/v1/messages), used to
 * back TechGarage's AI features (issue classification, the "Analyze with AI" problem-posting
 * assistant, etc).
 *
 * This is intentionally optional: the app must keep working with zero configuration.
 * - If {@code app.ai.anthropic-api-key} (env {@code ANTHROPIC_API_KEY}) is not set, {@link #isConfigured()}
 *   returns false and {@link #complete} is never called by well-behaved callers.
 * - If a call fails for any reason (network error, bad key, rate limit, timeout, malformed
 *   response), {@link #complete} returns {@code null} rather than throwing, so every caller in
 *   this codebase falls back to its own rule-based logic instead of breaking the request.
 */
@Slf4j
@Component
public class AnthropicClient {

    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.ai.anthropic-api-key:}")
    private String apiKey;

    @Value("${app.ai.anthropic-model:claude-haiku-4-5-20251001}")
    private String model;

    @Value("${app.ai.enabled:true}")
    private boolean aiFeaturesEnabled;

    public boolean isConfigured() {
        return aiFeaturesEnabled && apiKey != null && !apiKey.isBlank();
    }

    /**
     * Sends a single-turn prompt to Claude and returns the concatenated text of the response,
     * or {@code null} if the call could not be completed for any reason. Never throws.
     */
    public String complete(String systemPrompt, String userPrompt) {
        if (!isConfigured()) return null;
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 1024);
            body.put("system", systemPrompt);
            ArrayNode messages = body.putArray("messages");
            ObjectNode userMessage = messages.addObject();
            userMessage.put("role", "user");
            userMessage.put("content", userPrompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(25))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", ANTHROPIC_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.warn("Anthropic API returned HTTP {}: {}", response.statusCode(), truncate(response.body()));
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("content");
            StringBuilder text = new StringBuilder();
            if (content.isArray()) {
                for (JsonNode block : content) {
                    if ("text".equals(block.path("type").asText())) {
                        text.append(block.path("text").asText());
                    }
                }
            }
            return text.length() > 0 ? text.toString() : null;
        } catch (Exception e) {
            log.warn("Anthropic API call failed, caller will fall back to built-in logic: {}", e.getMessage());
            return null;
        }
    }

    private String truncate(String s) {
        if (s == null) return "";
        return s.length() > 300 ? s.substring(0, 300) + "…" : s;
    }
}
