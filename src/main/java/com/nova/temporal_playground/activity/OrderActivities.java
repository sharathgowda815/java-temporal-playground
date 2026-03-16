package com.nova.temporal_playground.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivities {

    @ActivityMethod
    void validateOrder(String orderId);

    @ActivityMethod
    void sendConfirmation(String orderId);
}
