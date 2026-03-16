package com.nova.temporal_playground.activity.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingActivitiesImplTest {

    private ShippingActivitiesImpl shippingActivities;

    @BeforeEach
    void setUp() {
        shippingActivities = new ShippingActivitiesImpl();
    }

    @Test
    void shipOrder_shouldReturnExpectedMessage() {
        String result = shippingActivities.shipOrder("order-123");
        assertEquals("Order shipped: order-123", result);
    }

    @Test
    void shipOrder_withDifferentOrderId_shouldReturnCorrectMessage() {
        String result = shippingActivities.shipOrder("order-abc-456");
        assertEquals("Order shipped: order-abc-456", result);
    }
}
