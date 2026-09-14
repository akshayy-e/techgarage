package com.techgarage.dto.job;

import com.techgarage.entity.ChangeRequestStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChangeRequestResponse {
    private Long id; private Long jobId; private Long requestedById; private String requestedByName;
    private String description; private Double additionalPrice; private Integer additionalDays;
    private ChangeRequestStatus status; private LocalDateTime createdAt; private LocalDateTime respondedAt;
}
