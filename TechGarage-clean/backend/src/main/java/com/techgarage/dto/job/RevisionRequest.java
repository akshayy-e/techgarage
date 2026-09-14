package com.techgarage.dto.job;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RevisionRequest {

    @NotBlank(message = "Revision notes cannot be empty")
    private String revisionNotes;
}
