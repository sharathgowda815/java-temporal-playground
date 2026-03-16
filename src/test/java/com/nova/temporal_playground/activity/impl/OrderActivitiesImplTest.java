package com.nova.temporal_playground.activity.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class OrderActivitiesImplTest {

    private OrderActivitiesImpl orderActivities;

    @BeforeEach
    void setUp() {
        orderActivities = new OrderActivitiesImpl();
    }

    @Test
    void validateOrder_shouldCompleteWithoutException() {
        assertDoesNotThrow(() -> orderActivities.validateOrder("order-123"));
    }

    @Test
    void validateOrder_withDifferentOrderId_shouldComplete() {
        assertDoesNotThrow(() -> orderActivities.validateOrder("order-abc-999"));
    }

    @Test
    void sendConfirmation_shouldCompleteWithoutException() {
        assertDoesNotThrow(() -> orderActivities.sendConfirmation("order-123"));
    }

    @Test
    void sendConfirmation_withDifferentOrderId_shouldComplete() {
        assertDoesNotThrow(() -> orderActivities.sendConfirmation("order-xyz-456"));
    }
}
