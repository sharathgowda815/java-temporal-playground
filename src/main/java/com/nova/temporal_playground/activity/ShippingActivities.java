package com.nova.temporal_playground.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ShippingActivities {

    @ActivityMethod
    String shipOrder(String orderId);
}
