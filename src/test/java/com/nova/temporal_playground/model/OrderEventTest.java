package com.nova.temporal_playground.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OrderEventTest {

    @Test
    void noArgsConstructor_shouldCreateEmptyEvent() {
        OrderEvent event = new OrderEvent();
        assertNull(event.getOrderId());
        assertNull(event.getCustomerId());
        assertNull(event.getAmount());
    }

    @Test
    void allArgsConstructor_shouldPopulateAllFields() {
        OrderEvent event = new OrderEvent("order-1", "customer-1", new BigDecimal("99.99"));
        assertEquals("order-1", event.getOrderId());
        assertEquals("customer-1", event.getCustomerId());
        assertEquals(new BigDecimal("99.99"), event.getAmount());
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        OrderEvent event = new OrderEvent();
        event.setOrderId("order-2");
        event.setCustomerId("customer-2");
        event.setAmount(new BigDecimal("150.50"));

        assertEquals("order-2", event.getOrderId());
        assertEquals("customer-2", event.getCustomerId());
        assertEquals(new BigDecimal("150.50"), event.getAmount());
    }

    @Test
    void equals_sameValues_shouldBeEqual() {
        OrderEvent event1 = new OrderEvent("order-1", "customer-1", new BigDecimal("100.0"));
        OrderEvent event2 = new OrderEvent("order-1", "customer-1", new BigDecimal("100.0"));
        assertEquals(event1, event2);
    }

    @Test
    void equals_differentValues_shouldNotBeEqual() {
        OrderEvent event1 = new OrderEvent("order-1", "customer-1", new BigDecimal("100.0"));
        OrderEvent event2 = new OrderEvent("order-2", "customer-1", new BigDecimal("100.0"));
        assertNotEquals(event1, event2);
    }

    @Test
    void hashCode_sameValues_shouldBeEqual() {
        OrderEvent event1 = new OrderEvent("order-1", "customer-1", new BigDecimal("100.0"));
        OrderEvent event2 = new OrderEvent("order-1", "customer-1", new BigDecimal("100.0"));
        assertEquals(event1.hashCode(), event2.hashCode());
    }

    @Test
    void toString_shouldContainFieldValues() {
        OrderEvent event = new OrderEvent("order-1", "customer-1", new BigDecimal("50.0"));
        String str = event.toString();
        assertTrue(str.contains("order-1"));
        assertTrue(str.contains("customer-1"));
        assertTrue(str.contains("50.0"));
    }
}
