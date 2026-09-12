package com.techgarage.dto.admin;

import com.techgarage.entity.Role;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Admin-facing user row. Unlike the raw User entity, this includes the freelancer's
 * verification status so the Users screen can actually reflect what "Verify" did
 * (previously the endpoint updated FreelancerProfile.verified, but the admin list
 * returned bare User entities that have no such field, so every freelancer looked
 * permanently unverified/un-actioned no matter how many times you clicked Verify).
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminUserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private boolean enabled;
    private LocalDateTime createdAt;

    /** Null for non-freelancers (clients/admins have no freelancer profile). */
    private Boolean verified;
}
