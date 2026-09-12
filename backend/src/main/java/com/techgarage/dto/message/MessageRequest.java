package com.techgarage.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MessageRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;
}
