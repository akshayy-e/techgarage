package com.techgarage.service;

import com.techgarage.dto.problem.ProblemAiSuggestionRequest;
import com.techgarage.dto.problem.ProblemAiSuggestionResponse;

/**
 * Powers the client-facing "Analyze with AI" button on the Post Problem form: given a draft
 * title/description, suggests category, technology, priority, a fair budget range, a cleaned-up
 * one-line summary, and a couple of clarifying questions freelancers will likely want answered.
 */
public interface AIAssistantService {
    ProblemAiSuggestionResponse suggest(ProblemAiSuggestionRequest request);
}
