package com.nova.temporal_playground.activity.impl;

import com.nova.temporal_playground.activity.InventoryActivities;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InventoryActivitiesImpl implements InventoryActivities {

    @Override
    public String reserveInventory(String orderId) {
        log.info("Reserving inventory for order: {}", orderId);
        return "Inventory reserved for order: " + orderId;
    }

    @Override
    public void releaseInventory(String orderId) {
        log.info("Releasing inventory for order: {}", orderId);
    }
}
