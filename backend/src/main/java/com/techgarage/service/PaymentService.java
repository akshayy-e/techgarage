package com.techgarage.service;

import com.techgarage.entity.Job;

public interface PaymentService {
    /** Prototype payment system: simulates holding funds when a job is assigned. */
    void holdPayment(Job job);
    /** Simulates releasing held funds to the freelancer once a job is completed. */
    void releasePayment(Job job);
}
