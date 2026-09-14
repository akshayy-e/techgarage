package com.techgarage.dto.payment;
import com.techgarage.entity.TransactionStatus;
import lombok.*;
import java.time.LocalDateTime;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransactionResponse {
    private Long id; private Long jobId; private Double amount; private Double platformFee; private Double freelancerAmount;
    private String currency; private String gatewayOrderId; private String gatewayPaymentId; private TransactionStatus status;
    private String failureReason; private String refundId; private LocalDateTime createdAt; private LocalDateTime capturedAt; private LocalDateTime refundedAt;
}
