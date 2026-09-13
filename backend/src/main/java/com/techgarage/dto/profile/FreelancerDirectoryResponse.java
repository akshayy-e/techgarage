package com.techgarage.dto.profile;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FreelancerDirectoryResponse {
    private Long userId;
    private String name;
    private String bio;
    private Integer experienceYears;
    private String skills;
    private String portfolio;
    private Double hourlyRate;
    private Boolean availability;
    private Boolean verified;
    private Boolean emailVerified;
    private Integer profileCompletion;
    private Double rating;
    private Integer totalReviews;
}
