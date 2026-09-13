package com.techgarage.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AIDiagnosisRequest {
    @NotBlank @Size(max = 200) private String title;
    @NotBlank @Size(max = 8000) private String description;
    @Size(max = 8000) private String logs;
    @Size(max = 1000) private String environment;
}
