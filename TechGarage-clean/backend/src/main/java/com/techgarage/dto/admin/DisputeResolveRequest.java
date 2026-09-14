package com.techgarage.dto.admin;

import com.techgarage.entity.DisputeStatus;
import com.techgarage.entity.DisputeResolutionAction;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DisputeResolveRequest {

    @NotNull
    private DisputeStatus status;

    private DisputeResolutionAction action;

    private String adminResponse;
}
