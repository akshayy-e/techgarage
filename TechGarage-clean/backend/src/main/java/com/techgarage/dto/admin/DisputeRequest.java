package com.techgarage.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DisputeRequest {

    @NotBlank(message = "Reason is required")
    private String reason;

    private String description;
}
