package com.nova.temporal_playground.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface InventoryActivities {

    @ActivityMethod
    String reserveInventory(String orderId);

    @ActivityMethod
    void releaseInventory(String orderId);
}
