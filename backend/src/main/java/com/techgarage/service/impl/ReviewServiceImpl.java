package com.techgarage.service.impl;

import com.techgarage.dto.review.ReviewRequest;
import com.techgarage.dto.review.ReviewResponse;
import com.techgarage.entity.FreelancerProfile;
import com.techgarage.entity.Job;
import com.techgarage.entity.JobStatus;
import com.techgarage.entity.Review;
import com.techgarage.entity.User;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.FreelancerProfileRepository;
import com.techgarage.repository.JobRepository;
import com.techgarage.repository.ReviewRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.NotificationService;
import com.techgarage.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final JobRepository jobRepository;
    private final FreelancerProfileRepository freelancerProfileRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;

    @Override
    public ReviewResponse submit(Long jobId, ReviewRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User me = securityUtil.getCurrentUser();

        if (!job.getClient().getId().equals(me.getId())) {
            throw new ForbiddenException("Only the client can leave a review for this job");
        }
        if (job.getStatus() != JobStatus.COMPLETED) {
            throw new BadRequestException("You can only review a completed job");
        }
        if (reviewRepository.findByJobId(jobId).isPresent()) {
            throw new BadRequestException("This job has already been reviewed");
        }

        Review review = Review.builder()
                .job(job)
                .client(job.getClient())
                .freelancer(job.getFreelancer())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();
        review = reviewRepository.save(review);

        updateFreelancerRating(job.getFreelancer().getId());

        notificationService.notify(job.getFreelancer().getId(),
                job.getClient().getName() + " left you a " + request.getRating() + "-star review");

        return toResponse(review);
    }

    @Override
    public List<ReviewResponse> getForFreelancer(Long freelancerId) {
        return reviewRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private void updateFreelancerRating(Long freelancerId) {
        List<Review> reviews = reviewRepository.findByFreelancerIdOrderByCreatedAtDesc(freelancerId);
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        FreelancerProfile profile = freelancerProfileRepository.findByUserId(freelancerId)
                .orElseThrow(() -> new ResourceNotFoundException("Freelancer profile not found"));
        profile.setRating(Math.round(avg * 10.0) / 10.0);
        profile.setTotalReviews(reviews.size());
        freelancerProfileRepository.save(profile);
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .jobId(r.getJob().getId())
                .clientId(r.getClient().getId())
                .clientName(r.getClient().getName())
                .freelancerId(r.getFreelancer().getId())
                .freelancerName(r.getFreelancer().getName())
                .rating(r.getRating())
                .comment(r.getComment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
