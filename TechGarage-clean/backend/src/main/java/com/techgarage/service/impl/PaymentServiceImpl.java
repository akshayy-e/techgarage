package com.techgarage.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgarage.dto.payment.*;
import com.techgarage.entity.*;
import com.techgarage.exception.BadRequestException;
import com.techgarage.exception.ForbiddenException;
import com.techgarage.exception.ResourceNotFoundException;
import com.techgarage.repository.JobRepository;
import com.techgarage.repository.PaymentTransactionRepository;
import com.techgarage.security.SecurityUtil;
import com.techgarage.service.NotificationService;
import com.techgarage.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final JobRepository jobRepository;
    private final PaymentTransactionRepository paymentRepository;
    private final SecurityUtil securityUtil;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.payment.enabled:false}") private boolean enabled;
    @Value("${app.payment.key-id:}") private String keyId;
    @Value("${app.payment.key-secret:}") private String keySecret;
    @Value("${app.payment.webhook-secret:}") private String webhookSecret;
    @Value("${app.payment.platform-fee-percent:10.0}") private double platformFeePercent;

    @Override @Transactional
    public PaymentOrderResponse createOrder(Long jobId) {
        if (!enabled || keyId.isBlank() || keySecret.isBlank()) throw new BadRequestException("Online payments are not configured yet");
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        User me = securityUtil.getCurrentUser();
        if (!job.getClient().getId().equals(me.getId())) throw new ForbiddenException("Only the client can pay for this job");
        if (job.getStatus() != JobStatus.ASSIGNED || job.getPaymentStatus() != PaymentStatus.PENDING) throw new BadRequestException("This job is not awaiting payment");
        if (paymentRepository.existsByJobIdAndStatus(jobId, TransactionStatus.CAPTURED)) throw new BadRequestException("This job is already paid");

        double capturedSoFar = paymentRepository.findByJobIdAndStatus(jobId, TransactionStatus.CAPTURED).stream()
                .mapToDouble(PaymentTransaction::getAmount).sum();
        double amount = money(job.getAgreedPrice() - capturedSoFar);
        if (amount <= 0) throw new BadRequestException("This job is already fully funded");
        double fee = money(amount * platformFeePercent / 100.0);
        double freelancerAmount = money(amount - fee);
        try {
            String body = "{\"amount\":" + Math.round(amount * 100) + ",\"currency\":\"INR\",\"receipt\":\"TG-JOB-" + jobId + "\",\"notes\":{\"jobId\":\"" + jobId + "\"}}";
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/orders"))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8)))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) throw new BadRequestException("Payment gateway could not create the order");
            JsonNode json = objectMapper.readTree(response.body());
            String orderId = json.path("id").asText();
            if (orderId.isBlank()) throw new BadRequestException("Payment gateway returned an invalid order");
            PaymentTransaction tx = paymentRepository.save(PaymentTransaction.builder().job(job).amount(amount).platformFee(fee).freelancerAmount(freelancerAmount).gatewayOrderId(orderId).build());
            return PaymentOrderResponse.builder().jobId(jobId).transactionId(tx.getId()).keyId(keyId).orderId(orderId).amount(amount).currency("INR").platformFee(fee).freelancerAmount(freelancerAmount).customerName(me.getName()).customerEmail(me.getEmail()).build();
        } catch (BadRequestException e) { throw e; } catch (Exception e) { throw new BadRequestException("Unable to create payment order"); }
    }

    @Override @Transactional
    public PaymentTransactionResponse verifyPayment(PaymentVerifyRequest request) {
        PaymentTransaction tx = paymentRepository.findByGatewayOrderId(request.getRazorpayOrderId()).orElseThrow(() -> new ResourceNotFoundException("Payment order not found"));
        User me = securityUtil.getCurrentUser();
        if (!tx.getJob().getClient().getId().equals(me.getId())) throw new ForbiddenException("You cannot verify this payment");
        if (tx.getStatus() == TransactionStatus.CAPTURED) return toResponse(tx);
        if (!secureEquals(hmac(request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(), keySecret), request.getRazorpaySignature())) throw new BadRequestException("Invalid payment signature");
        try {
            HttpRequest paymentRequest = HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/payments/" + request.getRazorpayPaymentId()))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8))).GET().build();
            HttpResponse<String> paymentResponse = HttpClient.newHttpClient().send(paymentRequest, HttpResponse.BodyHandlers.ofString());
            if (paymentResponse.statusCode() / 100 != 2) throw new BadRequestException("Unable to confirm payment with gateway");
            JsonNode paymentJson = objectMapper.readTree(paymentResponse.body());
            if (!"captured".equalsIgnoreCase(paymentJson.path("status").asText()) || !request.getRazorpayOrderId().equals(paymentJson.path("order_id").asText())) {
                throw new BadRequestException("Payment has not been captured for this order");
            }
        } catch (BadRequestException e) { throw e; } catch (Exception e) { throw new BadRequestException("Unable to confirm payment with gateway"); }
        tx.setGatewayPaymentId(request.getRazorpayPaymentId()); tx.setStatus(TransactionStatus.CAPTURED); tx.setCapturedAt(LocalDateTime.now());
        tx = paymentRepository.save(tx); holdPayment(tx.getJob());
        notificationService.notify(tx.getJob().getFreelancer().getId(), "Payment received for Job #" + tx.getJob().getId() + ". You can start work.");
        notificationService.notify(tx.getJob().getClient().getId(), "Payment confirmed for Job #" + tx.getJob().getId() + ".");
        return toResponse(tx);
    }

    @Override @Transactional
    public void handleWebhook(String payload, String signature) {
        if (!enabled || webhookSecret.isBlank()) throw new BadRequestException("Webhook payments are not configured");
        if (!secureEquals(hmac(payload, webhookSecret), signature)) throw new ForbiddenException("Invalid webhook signature");
        try {
            JsonNode root = objectMapper.readTree(payload); String event = root.path("event").asText();
            JsonNode payment = root.path("payload").path("payment").path("entity");
            String paymentId = payment.path("id").asText(); String orderId = payment.path("order_id").asText();
            if ("payment.captured".equals(event)) {
                PaymentTransaction tx = paymentRepository.findByGatewayOrderId(orderId).orElse(null);
                if (tx != null && tx.getStatus() != TransactionStatus.CAPTURED) { tx.setGatewayPaymentId(paymentId); tx.setStatus(TransactionStatus.CAPTURED); tx.setCapturedAt(LocalDateTime.now()); paymentRepository.save(tx); holdPayment(tx.getJob()); }
            } else if ("payment.failed".equals(event)) {
                PaymentTransaction tx = paymentRepository.findByGatewayOrderId(orderId).orElse(null);
                if (tx != null && tx.getStatus() == TransactionStatus.CREATED) { tx.setStatus(TransactionStatus.FAILED); tx.setFailureReason(payment.path("error_description").asText("Payment failed")); paymentRepository.save(tx); }
            } else if ("refund.processed".equals(event)) {
                String refundId = root.path("payload").path("refund").path("entity").path("id").asText();
                String refundedPaymentId = root.path("payload").path("refund").path("entity").path("payment_id").asText();
                if (!refundedPaymentId.isBlank()) { PaymentTransaction tx = paymentRepository.findByGatewayPaymentId(refundedPaymentId).orElse(null); if (tx != null) { tx.setStatus(TransactionStatus.REFUNDED); tx.setRefundId(refundId); tx.setRefundedAt(LocalDateTime.now()); paymentRepository.save(tx); } }
            }
        } catch (Exception e) { throw new BadRequestException("Invalid payment webhook payload"); }
    }

    @Override public List<PaymentTransactionResponse> getMyTransactions() {
        User me = securityUtil.getCurrentUser();
        if (me.getRole() == Role.CLIENT) return paymentRepository.findByJobClientIdOrderByCreatedAtDesc(me.getId()).stream().map(this::toResponse).collect(Collectors.toList());
        if (me.getRole() == Role.FREELANCER) return paymentRepository.findByJobFreelancerIdOrderByCreatedAtDesc(me.getId()).stream().map(this::toResponse).collect(Collectors.toList());
        return paymentRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override public void holdPayment(Job job) {
        double captured = paymentRepository.findByJobIdAndStatus(job.getId(), TransactionStatus.CAPTURED).stream()
                .mapToDouble(PaymentTransaction::getAmount).sum();
        if (captured + 0.005 >= job.getAgreedPrice()) {
            job.setPaymentStatus(PaymentStatus.HELD);
        } else {
            job.setPaymentStatus(PaymentStatus.PENDING);
        }
        jobRepository.save(job);
    }

    @Override @Transactional
    public void releasePayment(Job job) {
        if (job.getPaymentStatus() != PaymentStatus.HELD) throw new IllegalStateException("Only captured/held payments can be released");
        job.setPaymentStatus(PaymentStatus.RELEASED); jobRepository.save(job);
    }

    @Override
    public double getFreelancerNetAmount(Job job) {
        return paymentRepository.findFirstByJobIdAndStatusOrderByCreatedAtDesc(job.getId(), TransactionStatus.CAPTURED)
                .map(PaymentTransaction::getFreelancerAmount).orElse(0.0);
    }

    @Override @Transactional
    public void refundPayment(Job job) {
        if (job.getPaymentStatus() == PaymentStatus.RELEASED) throw new IllegalStateException("A released payment cannot be refunded");
        List<PaymentTransaction> captured = paymentRepository.findByJobIdAndStatus(job.getId(), TransactionStatus.CAPTURED);
        if (captured.isEmpty()) { job.setPaymentStatus(PaymentStatus.REFUNDED); jobRepository.save(job); return; }
        if (!enabled) throw new BadRequestException("Refund requires online payment configuration");
        try {
            HttpClient client = HttpClient.newHttpClient();
            String auth = "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8));
            for (PaymentTransaction tx : captured) {
                String body = "{\"amount\":" + Math.round(tx.getAmount() * 100) + "}";
                HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/payments/" + tx.getGatewayPaymentId() + "/refund"))
                        .header(HttpHeaders.AUTHORIZATION, auth).header(HttpHeaders.CONTENT_TYPE, "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body)).build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                if (res.statusCode() / 100 != 2) throw new BadRequestException("Payment refund failed");
                JsonNode json = objectMapper.readTree(res.body()); tx.setRefundId(json.path("id").asText()); tx.setStatus(TransactionStatus.REFUND_PENDING); paymentRepository.save(tx);
            }
            job.setPaymentStatus(PaymentStatus.REFUNDED); jobRepository.save(job);
        } catch (BadRequestException e) { throw e; } catch (Exception e) { throw new BadRequestException("Unable to process payment refund"); }
    }

    private PaymentTransactionResponse toResponse(PaymentTransaction p) { return PaymentTransactionResponse.builder().id(p.getId()).jobId(p.getJob().getId()).amount(p.getAmount()).platformFee(p.getPlatformFee()).freelancerAmount(p.getFreelancerAmount()).currency(p.getCurrency()).gatewayOrderId(p.getGatewayOrderId()).gatewayPaymentId(p.getGatewayPaymentId()).status(p.getStatus()).failureReason(p.getFailureReason()).refundId(p.getRefundId()).createdAt(p.getCreatedAt()).capturedAt(p.getCapturedAt()).refundedAt(p.getRefundedAt()).build(); }
    private double money(double v) { return Math.round(v * 100.0) / 100.0; }
    private String hmac(String value, String secret) { try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); byte[] out = mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); StringBuilder s = new StringBuilder(); for (byte b: out) s.append(String.format("%02x", b)); return s.toString(); } catch (Exception e) { throw new IllegalStateException("HMAC unavailable", e); } }
    private boolean secureEquals(String a, String b) { if (a == null || b == null) return false; return java.security.MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8)); }
}
