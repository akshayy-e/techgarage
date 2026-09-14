package com.techgarage.dto.problem;

import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;
import com.techgarage.entity.ProblemStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProblemResponse {
    private Long id;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private ProblemCategory category;
    private String technology;
    private Priority priority;
    private Double budget;
    private LocalDateTime expectedCompletionDate;
    private ProblemStatus status;
    private String attachmentUrl;
    private Long proposalCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
