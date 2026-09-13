package com.techgarage.dto.ai;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AIDiagnosisResponse {
    private String summary;
    private List<String> likelyCauses;
    private List<String> informationNeeded;
    private String suggestedNextStep;
    private double budgetMin;
    private double budgetMax;
    private boolean aiGenerated;
}
