package com.techgarage.repository;

import com.techgarage.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    List<Dispute> findAllByOrderByCreatedAtDesc();
    long countByStatus(com.techgarage.entity.DisputeStatus status);
}
