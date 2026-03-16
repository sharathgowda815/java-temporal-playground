package com.nova.temporal_playground.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface PaymentActivities {

    @ActivityMethod
    String processPayment(String orderId);

    @ActivityMethod
    void refundPayment(String orderId);
}
