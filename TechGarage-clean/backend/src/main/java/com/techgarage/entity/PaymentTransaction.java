package com.techgarage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_payment_job", columnList = "job_id"),
        @Index(name = "idx_payment_order", columnList = "gateway_order_id", unique = true),
        @Index(name = "idx_payment_id", columnList = "gateway_payment_id", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private Double platformFee;

    @Column(nullable = false)
    private Double freelancerAmount;

    @Builder.Default
    @Column(nullable = false, length = 3)
    private String currency = "INR";

    @Column(nullable = false, length = 40)
    private String gateway = "RAZORPAY";

    @Column(name = "gateway_order_id", nullable = false, unique = true)
    private String gatewayOrderId;

    @Column(name = "gateway_payment_id", unique = true)
    private String gatewayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TransactionStatus status = TransactionStatus.CREATED;

    private String failureReason;
    private String refundId;
    private LocalDateTime createdAt;
    private LocalDateTime capturedAt;
    private LocalDateTime refundedAt;

    @PrePersist protected void onCreate() { createdAt = LocalDateTime.now(); }
}
