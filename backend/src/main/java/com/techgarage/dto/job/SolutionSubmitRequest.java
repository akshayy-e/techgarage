package com.techgarage.dto.job;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SolutionSubmitRequest {

    @NotBlank(message = "Solution notes cannot be empty")
    private String solutionNotes;
}
