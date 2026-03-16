package com.nova.temporal_playground.activity.impl;

import com.nova.temporal_playground.activity.ShippingActivities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShippingActivitiesImpl implements ShippingActivities {

    @Override
    public String shipOrder(String orderId) {
        log.info("Shipping order: {}", orderId);
        return "Order shipped: " + orderId;
    }
}
