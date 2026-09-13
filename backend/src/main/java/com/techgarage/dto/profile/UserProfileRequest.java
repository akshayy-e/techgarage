package com.techgarage.dto.profile;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserProfileRequest {
    private String name;
    private String phone;
}
