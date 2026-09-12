package com.techgarage.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "freelancer_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FreelancerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 2000)
    private String bio;

    private Integer experienceYears;

    @Column(length = 1000)
    private String skills; // comma-separated for MVP simplicity

    @Column(length = 2000)
    private String portfolio;

    private Double hourlyRate;

    @Builder.Default
    private Boolean availability = true;

    @Builder.Default
    private Boolean verified = false;

    @Builder.Default
    private Double rating = 0.0;

    @Builder.Default
    private Integer totalReviews = 0;

    @Builder.Default
    private Double totalEarnings = 0.0;
}
