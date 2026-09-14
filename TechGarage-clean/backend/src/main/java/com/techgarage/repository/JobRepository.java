package com.techgarage.repository;

import com.techgarage.entity.Job;
import com.techgarage.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<Job> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    Optional<Job> findByProblemId(Long problemId);
    boolean existsByProblemId(Long problemId);
    long countByClientIdAndStatus(Long clientId, JobStatus status);
    long countByFreelancerIdAndStatus(Long freelancerId, JobStatus status);
    long countByStatus(JobStatus status);
    List<Job> findAllByOrderByCreatedAtDesc();
}
