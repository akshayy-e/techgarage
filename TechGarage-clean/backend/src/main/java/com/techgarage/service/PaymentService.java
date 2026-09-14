package com.techgarage.service;
import com.techgarage.dto.payment.PaymentOrderResponse;
import com.techgarage.dto.payment.PaymentTransactionResponse;
import com.techgarage.dto.payment.PaymentVerifyRequest;
import com.techgarage.entity.Job;
import java.util.List;
public interface PaymentService {
    PaymentOrderResponse createOrder(Long jobId);
    PaymentTransactionResponse verifyPayment(PaymentVerifyRequest request);
    void handleWebhook(String payload, String signature);
    List<PaymentTransactionResponse> getMyTransactions();
    void holdPayment(Job job);
    void releasePayment(Job job);
    void refundPayment(Job job);
    double getFreelancerNetAmount(Job job);
}
