package com.nova.temporal_playground.activity.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentActivitiesImplTest {

    private PaymentActivitiesImpl paymentActivities;

    @BeforeEach
    void setUp() {
        paymentActivities = new PaymentActivitiesImpl();
    }

    @RepeatedTest(20)
    void processPayment_shouldEventuallySucceedOrFail() {
        // Since ~33% fail randomly, run enough times to exercise both paths.
        // Each invocation either returns a result or throws RuntimeException.
        try {
            String result = paymentActivities.processPayment("order-123");
            assertEquals("Payment completed for order: order-123", result);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Simulated payment failure"));
        }
    }

    @Test
    void processPayment_successResult_containsOrderId() {
        // Run until we get a successful result to verify format
        String result = null;
        for (int i = 0; i < 100; i++) {
            try {
                result = paymentActivities.processPayment("order-456");
                break;
            } catch (RuntimeException ignored) {
            }
        }
        assertNotNull(result, "Expected at least one successful payment in 100 attempts");
        assertEquals("Payment completed for order: order-456", result);
    }

    @Test
    void processPayment_failure_throwsRuntimeException() {
        // Run until we get a failure to verify the exception message
        RuntimeException caught = null;
        for (int i = 0; i < 100; i++) {
            try {
                paymentActivities.processPayment("order-789");
            } catch (RuntimeException e) {
                caught = e;
                break;
            }
        }
        assertNotNull(caught, "Expected at least one failure in 100 attempts");
        assertEquals("Simulated payment failure for order: order-789", caught.getMessage());
    }

    @Test
    void refundPayment_shouldCompleteWithoutException() {
        assertDoesNotThrow(() -> paymentActivities.refundPayment("order-123"));
    }
}
