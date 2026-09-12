package com.techgarage.service;

import com.techgarage.dto.review.ReviewRequest;
import com.techgarage.dto.review.ReviewResponse;
import java.util.List;

public interface ReviewService {
    ReviewResponse submit(Long jobId, ReviewRequest request);
    List<ReviewResponse> getForFreelancer(Long freelancerId);
}
