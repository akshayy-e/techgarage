package com.techgarage.repository;

import com.techgarage.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByJobIdOrderByCreatedAtAsc(Long jobId);
}
