package com.techgarage.dto.invitation;

import com.techgarage.entity.InvitationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InvitationResponse {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private Long clientId;
    private String clientName;
    private Long freelancerId;
    private String freelancerName;
    private InvitationStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
