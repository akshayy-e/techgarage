package com.techgarage.dto.review;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewResponse {
    private Long id;
    private Long jobId;
    private Long clientId;
    private String clientName;
    private Long freelancerId;
    private String freelancerName;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
