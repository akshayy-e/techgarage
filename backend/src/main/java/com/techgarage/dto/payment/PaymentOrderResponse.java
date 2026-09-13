package com.techgarage.dto.payment;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentOrderResponse {
    private Long jobId;
    private Long transactionId;
    private String keyId;
    private String orderId;
    private Double amount;
    private String currency;
    private Double platformFee;
    private Double freelancerAmount;
    private String customerName;
    private String customerEmail;
}
