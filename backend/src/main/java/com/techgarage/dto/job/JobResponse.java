package com.techgarage.dto.job;

import com.techgarage.entity.JobStatus;
import com.techgarage.entity.PaymentStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class JobResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private Long clientId;
    private String clientName;
    private Long freelancerId;
    private String freelancerName;
    private Double agreedPrice;
    private JobStatus status;
    private PaymentStatus paymentStatus;
    private String solutionNotes;
    private String revisionNotes;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
