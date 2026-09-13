package com.techgarage.service;

import com.techgarage.dto.profile.FreelancerDirectoryResponse;
import java.util.List;

public interface FreelancerDirectoryService {
    List<FreelancerDirectoryResponse> search(String query, Boolean verified, Boolean available, Double minRating, String technology);
}
