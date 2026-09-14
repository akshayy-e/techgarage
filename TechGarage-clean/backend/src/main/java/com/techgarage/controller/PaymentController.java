package com.techgarage.controller;

import com.techgarage.dto.common.ApiResponse;
import com.techgarage.dto.payment.PaymentOrderResponse;
import com.techgarage.dto.payment.PaymentTransactionResponse;
import com.techgarage.dto.payment.PaymentVerifyRequest;
import com.techgarage.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/jobs/{jobId}/order")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(@PathVariable Long jobId) {
        return ResponseEntity.status(201).body(ApiResponse.ok("Payment order created", paymentService.createOrder(jobId)));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<PaymentTransactionResponse>> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Payment verified", paymentService.verifyPayment(request)));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestHeader(value = "X-Razorpay-Signature", required = false) String signature,
                                        @RequestBody String payload) {
        paymentService.handleWebhook(payload, signature == null ? "" : signature);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mine")
    public ResponseEntity<ApiResponse<List<PaymentTransactionResponse>>> mine() {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getMyTransactions()));
    }
}
