package com.techgarage.repository;

import com.techgarage.entity.FreelancerInvitation;
import com.techgarage.entity.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FreelancerInvitationRepository extends JpaRepository<FreelancerInvitation, Long> {
    List<FreelancerInvitation> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    List<FreelancerInvitation> findByClientIdOrderByCreatedAtDesc(Long clientId);
    Optional<FreelancerInvitation> findByProblemIdAndFreelancerId(Long problemId, Long freelancerId);
    boolean existsByProblemIdAndFreelancerIdAndStatus(Long problemId, Long freelancerId, InvitationStatus status);
}
