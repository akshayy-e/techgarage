package com.techgarage.dto.problem;

import com.techgarage.entity.Priority;
import com.techgarage.entity.ProblemCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProblemRequest {

    @NotBlank(message = "Title cannot be empty")
    private String title;

    @NotBlank(message = "Description cannot be empty")
    private String description;

    private ProblemCategory category;

    private String technology;

    private Priority priority;

    @Positive(message = "Budget must be positive")
    private Double budget;

    private LocalDateTime expectedCompletionDate;

    private String attachmentUrl;
}
