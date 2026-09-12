package com.techgarage.dto.job;

import com.techgarage.entity.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class JobStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private JobStatus status;

    private String notes;
}
