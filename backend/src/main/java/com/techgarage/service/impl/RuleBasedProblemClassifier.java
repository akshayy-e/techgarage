package com.techgarage.service.impl;

import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;

/**
 * Keyword-based classifier shared by {@link AIClassificationServiceImpl} and
 * {@link AIAssistantServiceImpl}. This is the original TechGarage MVP classifier — kept as the
 * always-available fallback so both AI-backed services keep working with zero configuration, and
 * so the two never drift out of sync with each other.
 */
final class RuleBasedProblemClassifier {

    private RuleBasedProblemClassifier() {
    }

    static ProblemCategory detectCategory(String text) {
        if (containsAny(text, "database", "sql", "mysql", "postgres", "query", "migration")) return ProblemCategory.DATABASE;
        if (containsAny(text, "api", "endpoint", "rest", "graphql")) return ProblemCategory.API;
        if (containsAny(text, "aws", "cloud", "s3", "ec2", "azure", "gcp", "deploy")) return ProblemCategory.CLOUD;
        if (containsAny(text, "docker", "kubernetes", "ci/cd", "pipeline", "jenkins")) return ProblemCategory.DEVOPS;
        if (containsAny(text, "android", "ios", "flutter", "react native", "mobile app")) return ProblemCategory.MOBILE;
        if (containsAny(text, "ui", "css", "layout", "design", "responsive", "ux")) return ProblemCategory.UI_UX;
        if (containsAny(text, "auth", "login", "jwt", "oauth", "security", "vulnerability")) return ProblemCategory.SECURITY;
        if (containsAny(text, "model", "ml", "ai", "tensorflow", "pytorch", "llm")) return ProblemCategory.AI_ML;
        if (containsAny(text, "spring boot", "java", "node", "django", "flask", "backend", "server", "nullpointerexception", "api call")) return ProblemCategory.BACKEND;
        if (containsAny(text, "react", "angular", "vue", "frontend", "javascript", "html", "css")) return ProblemCategory.FRONTEND;
        return ProblemCategory.OTHER;
    }

    static String detectTechnology(String text) {
        if (containsAny(text, "spring boot", "spring")) return "Java / Spring Boot";
        if (containsAny(text, "react")) return "React";
        if (containsAny(text, "angular")) return "Angular";
        if (containsAny(text, "vue")) return "Vue.js";
        if (containsAny(text, "node")) return "Node.js";
        if (containsAny(text, "django", "flask", "python")) return "Python";
        if (containsAny(text, "mysql")) return "MySQL";
        if (containsAny(text, "postgres")) return "PostgreSQL";
        if (containsAny(text, "aws")) return "AWS";
        if (containsAny(text, "docker")) return "Docker";
        if (containsAny(text, "flutter")) return "Flutter";
        if (containsAny(text, "android")) return "Android";
        if (containsAny(text, "ios")) return "iOS";
        return "General";
    }

    static Priority detectPriority(String text) {
        if (containsAny(text, "production down", "urgent", "critical", "emergency", "outage", "data loss"))
            return Priority.EMERGENCY;
        if (containsAny(text, "asap", "important", "blocking", "high priority"))
            return Priority.HIGH;
        if (containsAny(text, "minor", "cosmetic", "whenever", "low priority"))
            return Priority.LOW;
        return Priority.MEDIUM;
    }

    static boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}
