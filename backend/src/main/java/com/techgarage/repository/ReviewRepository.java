package com.techgarage.repository;

import com.techgarage.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    Optional<Review> findByJobId(Long jobId);
}
