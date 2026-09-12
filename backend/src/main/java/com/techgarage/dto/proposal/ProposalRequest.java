package com.techgarage.dto.proposal;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProposalRequest {

    @Positive(message = "Proposal price must be positive")
    private Double price;

    @Positive(message = "Estimated days must be positive")
    private Integer estimatedDays;

    private String message;
}
