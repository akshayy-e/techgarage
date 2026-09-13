package com.techgarage.dto.job;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChangeRequestCreate {
    @NotBlank @Size(max=2000) private String description;
    @NotNull @DecimalMin(value="0.0", inclusive=true) private Double additionalPrice;
    @NotNull @Min(0) @Max(365) private Integer additionalDays;
}
