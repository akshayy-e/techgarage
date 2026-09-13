package com.techgarage.repository;
import com.techgarage.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ChangeRequestRepository extends JpaRepository<ChangeRequest,Long> {
    List<ChangeRequest> findByJobIdOrderByCreatedAtDesc(Long jobId);
    boolean existsByJobIdAndStatus(Long jobId, ChangeRequestStatus status);
}
