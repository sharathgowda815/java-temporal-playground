package com.nova.temporal_playground.activity.impl;

import com.nova.temporal_playground.activity.OrderActivities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OrderActivitiesImpl implements OrderActivities {

    @Override
    public void validateOrder(String orderId) {
        log.info("Validating order: {}", orderId);
    }

    @Override
    public void sendConfirmation(String orderId) {
        log.info("Sending confirmation for order: {}", orderId);
    }
}
