package com.techgarage.service;

import com.techgarage.dto.job.*;
import java.util.List;

public interface JobService {
    List<JobResponse> getMyJobs();
    JobResponse getById(Long id);
    JobResponse updateStatus(Long id, JobStatusUpdateRequest request);
    JobResponse submitSolution(Long id, SolutionSubmitRequest request);
    JobResponse requestRevision(Long id, RevisionRequest request);
    JobResponse complete(Long id);
    JobResponse cancel(Long id);
}
