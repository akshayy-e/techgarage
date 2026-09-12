package com.techgarage.dto.admin;

import com.techgarage.entity.DisputeStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DisputeResolveRequest {

    @NotNull
    private DisputeStatus status;

    private String adminResponse;
}
