package com.techgarage.dto.proposal;

import com.techgarage.entity.ProposalStatus;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProposalResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private Long freelancerId;
    private String freelancerName;
    private Double freelancerRating;
    private String freelancerSkills;
    private Integer freelancerExperience;
    private Double price;
    private Integer estimatedDays;
    private String message;
    private ProposalStatus status;
    private LocalDateTime createdAt;
}
