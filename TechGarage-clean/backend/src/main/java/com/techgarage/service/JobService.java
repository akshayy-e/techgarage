package com.techgarage.service;

import com.techgarage.dto.job.*;
import java.util.List;
import com.techgarage.dto.job.ChangeRequestCreate;
import com.techgarage.dto.job.ChangeRequestResponse;

public interface JobService {
    List<JobResponse> getMyJobs();
    JobResponse getById(Long id);
    JobResponse updateStatus(Long id, JobStatusUpdateRequest request);
    JobResponse submitSolution(Long id, SolutionSubmitRequest request);
    JobResponse requestRevision(Long id, RevisionRequest request);
    JobResponse complete(Long id);
    JobResponse cancel(Long id);
    List<ChangeRequestResponse> getChangeRequests(Long jobId);
    ChangeRequestResponse createChangeRequest(Long jobId, ChangeRequestCreate request);
    ChangeRequestResponse respondToChangeRequest(Long id, boolean accept);
}
