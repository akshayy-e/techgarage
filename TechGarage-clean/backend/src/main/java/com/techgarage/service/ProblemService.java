package com.techgarage.service;

import com.techgarage.dto.problem.ProblemAiSuggestionRequest;
import com.techgarage.dto.problem.ProblemAiSuggestionResponse;
import com.techgarage.dto.problem.ProblemRequest;
import com.techgarage.dto.problem.ProblemResponse;
import java.util.List;

public interface ProblemService {
    ProblemResponse create(ProblemRequest request);
    ProblemAiSuggestionResponse getAiSuggestion(ProblemAiSuggestionRequest request);
    ProblemResponse getById(Long id);
    List<ProblemResponse> getAllOpen();
    List<ProblemResponse> getEmergencyOpen();
    List<ProblemResponse> getMyProblems();
    ProblemResponse update(Long id, ProblemRequest request);
    void delete(Long id);
}
