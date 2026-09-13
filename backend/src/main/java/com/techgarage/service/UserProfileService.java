package com.techgarage.service;

import com.techgarage.dto.profile.UserProfileRequest;
import com.techgarage.dto.profile.UserProfileResponse;

public interface UserProfileService {
    UserProfileResponse getMine();
    UserProfileResponse update(UserProfileRequest request);
}
