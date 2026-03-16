package com.nova.temporal_playground.activity.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryActivitiesImplTest {

    private InventoryActivitiesImpl inventoryActivities;

    @BeforeEach
    void setUp() {
        inventoryActivities = new InventoryActivitiesImpl();
    }

    @Test
    void reserveInventory_shouldReturnExpectedMessage() {
        String result = inventoryActivities.reserveInventory("order-123");
        assertEquals("Inventory reserved for order: order-123", result);
    }

    @Test
    void reserveInventory_withDifferentOrderId_shouldReturnCorrectMessage() {
        String result = inventoryActivities.reserveInventory("order-xyz-999");
        assertEquals("Inventory reserved for order: order-xyz-999", result);
    }

    @Test
    void releaseInventory_shouldCompleteWithoutException() {
        assertDoesNotThrow(() -> inventoryActivities.releaseInventory("order-123"));
    }
}
