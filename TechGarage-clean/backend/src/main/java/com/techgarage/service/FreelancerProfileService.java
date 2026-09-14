package com.techgarage.service;

import com.techgarage.dto.profile.FreelancerProfileRequest;
import com.techgarage.dto.profile.FreelancerProfileResponse;
import java.util.List;

public interface FreelancerProfileService {
    FreelancerProfileResponse getMyProfile();
    FreelancerProfileResponse getByUserId(Long userId);
    FreelancerProfileResponse update(FreelancerProfileRequest request);
    List<FreelancerProfileResponse> getAll();
}
