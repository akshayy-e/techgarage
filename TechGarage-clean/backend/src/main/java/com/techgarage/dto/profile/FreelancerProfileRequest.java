package com.techgarage.dto.profile;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FreelancerProfileRequest {
    private String bio;
    private Integer experienceYears;
    private String skills;
    private String portfolio;
    private Double hourlyRate;
    private Boolean availability;
}
