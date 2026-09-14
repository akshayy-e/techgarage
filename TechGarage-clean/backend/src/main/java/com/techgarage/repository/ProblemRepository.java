package com.techgarage.repository;

import com.techgarage.entity.Problem;
import com.techgarage.entity.ProblemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {
    List<Problem> findByClientIdOrderByCreatedAtDesc(Long clientId);
    List<Problem> findByStatusOrderByCreatedAtDesc(ProblemStatus status);
    List<Problem> findAllByOrderByCreatedAtDesc();
    long countByClientId(Long clientId);
    long countByStatus(ProblemStatus status);
}
