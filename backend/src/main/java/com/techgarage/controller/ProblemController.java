package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.problem.ProblemAiSuggestionRequest;
import com.techgarage.dto.problem.ProblemAiSuggestionResponse;
import com.techgarage.dto.problem.ProblemRequest;
import com.techgarage.dto.problem.ProblemResponse;
import com.techgarage.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProblemResponse>> create(@Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Problem posted successfully", problemService.create(request)));
    }

    // Preview-only: given a draft title/description, returns AI-suggested category, technology,
    // priority, budget range, a cleaned-up summary, and clarifying questions. Nothing is saved —
    // the client applies (or ignores) the suggestion before submitting the form via POST above.
    @PostMapping("/ai-suggest")
    public ResponseEntity<ApiResponse<ProblemAiSuggestionResponse>> aiSuggest(@Valid @RequestBody ProblemAiSuggestionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getAiSuggestion(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getAllOpen() {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getAllOpen()));
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getMine() {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getMyProblems()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(problemService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> update(@PathVariable Long id, @Valid @RequestBody ProblemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Problem updated", problemService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> delete(@PathVariable Long id) {
        problemService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Problem deleted", null));
    }
}
