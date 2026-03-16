package com.nova.temporal_playground.workflow.payment;

import com.nova.temporal_playground.activity.PaymentActivities;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PaymentWorkflowTest {

    private static final String TASK_QUEUE = "payment-test-queue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;
    private PaymentActivities paymentActivities;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        paymentActivities = mock(PaymentActivities.class, withSettings().withoutAnnotations());
        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(PaymentWorkflowImpl.class);
        worker.registerActivitiesImplementations(paymentActivities);
        testEnv.start();
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void processPayment_success_shouldReturnResult() {
        when(paymentActivities.processPayment("order-123"))
                .thenReturn("Payment completed for order: order-123");

        PaymentWorkflow workflow = client.newWorkflowStub(
                PaymentWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.processPayment("order-123");

        assertEquals("Payment completed for order: order-123", result);
        verify(paymentActivities).processPayment("order-123");
    }

    @Test
    void processPayment_activityReturnsCustomMessage_shouldPropagate() {
        when(paymentActivities.processPayment("order-456"))
                .thenReturn("Custom payment result");

        PaymentWorkflow workflow = client.newWorkflowStub(
                PaymentWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.processPayment("order-456");

        assertEquals("Custom payment result", result);
    }
}
