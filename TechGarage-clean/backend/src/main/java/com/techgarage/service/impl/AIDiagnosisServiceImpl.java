package com.techgarage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgarage.dto.ai.AIDiagnosisRequest;
import com.techgarage.dto.ai.AIDiagnosisResponse;
import com.techgarage.service.AIDiagnosisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Slf4j @Service @RequiredArgsConstructor
public class AIDiagnosisServiceImpl implements AIDiagnosisService {
    private final AnthropicClient anthropicClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override public AIDiagnosisResponse diagnose(AIDiagnosisRequest r) {
        if (anthropicClient.isConfigured()) {
            try {
                String system = "You are TechGarage's software repair triage assistant. Return ONLY JSON with keys " +
                        "summary, likelyCauses (array of 3), informationNeeded (array of 3), suggestedNextStep, budgetMin, budgetMax. " +
                        "Do not claim certainty. Budgets are USD estimates. Never ask for passwords, API keys, tokens, or secrets.";
                String user = "Title: " + safe(r.getTitle()) + "\nDescription: " + safe(r.getDescription()) +
                        "\nLogs: " + safe(r.getLogs()) + "\nEnvironment: " + safe(r.getEnvironment());
                String raw = anthropicClient.complete(system, user);
                JsonNode n = mapper.readTree(strip(raw));
                List<String> causes = array(n.path("likelyCauses"));
                List<String> info = array(n.path("informationNeeded"));
                if (!causes.isEmpty()) return AIDiagnosisResponse.builder().summary(n.path("summary").asText("Initial technical diagnosis"))
                        .likelyCauses(causes).informationNeeded(info).suggestedNextStep(n.path("suggestedNextStep").asText("Collect the requested logs and reproduce the issue."))
                        .budgetMin(Math.max(0,n.path("budgetMin").asDouble(50))).budgetMax(Math.max(0,n.path("budgetMax").asDouble(250))).aiGenerated(true).build();
            } catch (Exception e) { log.warn("AI diagnosis fallback: {}", e.getMessage()); }
        }
        return fallback(r);
    }
    private AIDiagnosisResponse fallback(AIDiagnosisRequest r) {
        String t=(safe(r.getTitle())+" "+safe(r.getDescription())+" "+safe(r.getLogs())).toLowerCase();
        List<String> causes=new ArrayList<>();
        if(t.contains("500")||t.contains("exception")||t.contains("spring")) { causes.add("Backend exception or configuration issue"); causes.add("Environment variable or database configuration mismatch"); causes.add("Unhandled request/validation error"); }
        else if(t.contains("slow")||t.contains("timeout")) { causes.add("Slow database query or missing index"); causes.add("External API/network latency"); causes.add("Insufficient server resources"); }
        else { causes.add("Application configuration or environment mismatch"); causes.add("Recent code/dependency change"); causes.add("Unhandled application error"); }
        return AIDiagnosisResponse.builder().summary("Initial triage only; a mechanic should inspect the logs and reproduce the issue.")
                .likelyCauses(causes).informationNeeded(List.of("Exact error/log output", "Steps to reproduce", "Environment and recent changes"))
                .suggestedNextStep("Do not share passwords, API keys, tokens, or private credentials. Share sanitized logs and reproduction steps.")
                .budgetMin(50).budgetMax(250).aiGenerated(false).build();
    }
    private List<String> array(JsonNode n){List<String> x=new ArrayList<>(); if(n.isArray()) n.forEach(v->{String s=v.asText(null);if(s!=null&&!s.isBlank())x.add(s);}); return x;}
    private String strip(String s){if(s==null)return "";String x=s.trim();if(x.startsWith("```"))x=x.replaceFirst("^```[a-zA-Z]*\\s*","").replaceFirst("```\\s*$","");return x.trim();}
    private String safe(String s){return s==null?"":s;}
}
