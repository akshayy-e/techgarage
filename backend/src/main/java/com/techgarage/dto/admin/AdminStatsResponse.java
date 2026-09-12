package com.techgarage.dto.admin;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminStatsResponse {
    private long totalUsers;
    private long totalFreelancers;
    private long totalClients;
    private long totalProblems;
    private long activeJobs;
    private long completedJobs;
    private long totalDisputes;
    private long openDisputes;
}
