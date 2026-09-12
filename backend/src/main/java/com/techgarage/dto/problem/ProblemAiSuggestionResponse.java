package com.techgarage.dto.problem;

import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;
import lombok.*;

import java.util.List;

/**
 * Preview shown to a client on the "Post a technical problem" form before they submit, from the
 * {@code POST /api/problems/ai-suggest} endpoint. Purely advisory — the client can apply, tweak,
 * or ignore it; nothing here is persisted until they submit the form.
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProblemAiSuggestionResponse {
    private ProblemCategory category;
    private String technology;
    private Priority priority;
    private Double suggestedBudgetMin;
    private Double suggestedBudgetMax;
    private String summary;
    private List<String> clarifyingQuestions;

    /** True when a real Claude call produced this suggestion; false when the built-in heuristic fallback was used. */
    private boolean aiGenerated;
}
