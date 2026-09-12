package com.techgarage.service.impl;

import com.techgarage.entity.Job;
import com.techgarage.entity.PaymentStatus;
import com.techgarage.service.PaymentService;
import org.springframework.stereotype.Service;

/**
 * Prototype Payment System — simulates an escrow-style flow (Pending -> Held -> Released)
 * without a real payment gateway. Swap this implementation for Stripe/Razorpay in production
 * by implementing PaymentService against their SDK; callers (JobServiceImpl) do not change.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Override
    public void holdPayment(Job job) {
        job.setPaymentStatus(PaymentStatus.HELD);
    }

    @Override
    public void releasePayment(Job job) {
        job.setPaymentStatus(PaymentStatus.RELEASED);
    }
}
