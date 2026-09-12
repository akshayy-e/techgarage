package com.techgarage.repository;

import com.techgarage.entity.Proposal;
import com.techgarage.entity.ProposalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByProblemIdOrderByCreatedAtDesc(Long problemId);
    List<Proposal> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    Optional<Proposal> findByProblemIdAndFreelancerId(Long problemId, Long freelancerId);
    List<Proposal> findByProblemIdAndStatus(Long problemId, ProposalStatus status);
    long countByFreelancerIdAndStatus(Long freelancerId, ProposalStatus status);
}
