package com.techgarage.service;

import com.techgarage.dto.problem.ProblemRequest;

public interface AIClassificationService {
    /** Enriches a problem request with an auto-detected category/technology/priority when left unspecified. */
    ProblemRequest classify(ProblemRequest request);
}
