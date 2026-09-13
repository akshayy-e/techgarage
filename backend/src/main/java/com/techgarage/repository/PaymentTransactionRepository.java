package com.techgarage.repository;
import com.techgarage.entity.PaymentTransaction;
import com.techgarage.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByGatewayOrderId(String gatewayOrderId);
    Optional<PaymentTransaction> findByGatewayPaymentId(String gatewayPaymentId);
    Optional<PaymentTransaction> findFirstByJobIdAndStatusOrderByCreatedAtDesc(Long jobId, TransactionStatus status);
    List<PaymentTransaction> findByJobClientIdOrderByCreatedAtDesc(Long clientId);
    List<PaymentTransaction> findByJobFreelancerIdOrderByCreatedAtDesc(Long freelancerId);
    boolean existsByJobIdAndStatus(Long jobId, TransactionStatus status);
    List<PaymentTransaction> findByJobIdAndStatus(Long jobId, TransactionStatus status);
}
