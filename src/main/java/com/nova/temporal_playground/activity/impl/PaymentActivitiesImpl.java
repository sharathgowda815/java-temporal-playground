package com.nova.temporal_playground.activity.impl;

import com.nova.temporal_playground.activity.PaymentActivities;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

@Slf4j
public class PaymentActivitiesImpl implements PaymentActivities {

    private final Random random = new Random();

    @Override
    public String processPayment(String orderId) {
        log.info("Processing payment for order: {}", orderId);
        if (random.nextInt(3) == 0) {
            log.warn("Payment failed for order: {} -- will be retried by Temporal", orderId);
            throw new RuntimeException("Simulated payment failure for order: " + orderId);
        }
        log.info("Payment processed successfully for order: {}", orderId);
        return "Payment completed for order: " + orderId;
    }

    @Override
    public void refundPayment(String orderId) {
        log.info("Refunding payment for order: {}", orderId);
    }
}
