package com.nova.temporal_playground.workflow.shipping;

import com.nova.temporal_playground.activity.ShippingActivities;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ShippingWorkflowTest {

    private static final String TASK_QUEUE = "shipping-test-queue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;
    private ShippingActivities shippingActivities;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        shippingActivities = mock(ShippingActivities.class, withSettings().withoutAnnotations());
        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(ShippingWorkflowImpl.class);
        worker.registerActivitiesImplementations(shippingActivities);
        testEnv.start();
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void shipOrder_success_shouldReturnResult() {
        when(shippingActivities.shipOrder("order-123"))
                .thenReturn("Order shipped: order-123");

        ShippingWorkflow workflow = client.newWorkflowStub(
                ShippingWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.shipOrder("order-123");

        assertEquals("Order shipped: order-123", result);
        verify(shippingActivities).shipOrder("order-123");
    }

    @Test
    void shipOrder_activityReturnsCustomMessage_shouldPropagate() {
        when(shippingActivities.shipOrder("order-456"))
                .thenReturn("Custom shipping result");

        ShippingWorkflow workflow = client.newWorkflowStub(
                ShippingWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.shipOrder("order-456");

        assertEquals("Custom shipping result", result);
    }
}
