package com.techgarage.dto.invitation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class InvitationRequest {
    @NotNull
    private Long problemId;

    @Size(max = 1000)
    private String message;
}
