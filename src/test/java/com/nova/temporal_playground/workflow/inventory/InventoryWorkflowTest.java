package com.nova.temporal_playground.workflow.inventory;

import com.nova.temporal_playground.activity.InventoryActivities;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class InventoryWorkflowTest {

    private static final String TASK_QUEUE = "inventory-test-queue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;
    private InventoryActivities inventoryActivities;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();
        inventoryActivities = mock(InventoryActivities.class, withSettings().withoutAnnotations());
        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(InventoryWorkflowImpl.class);
        worker.registerActivitiesImplementations(inventoryActivities);
        testEnv.start();
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void reserveInventory_success_shouldReturnResult() {
        when(inventoryActivities.reserveInventory("order-123"))
                .thenReturn("Inventory reserved for order: order-123");

        InventoryWorkflow workflow = client.newWorkflowStub(
                InventoryWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.reserveInventory("order-123");

        assertEquals("Inventory reserved for order: order-123", result);
        verify(inventoryActivities).reserveInventory("order-123");
    }

    @Test
    void reserveInventory_activityReturnsCustomMessage_shouldPropagate() {
        when(inventoryActivities.reserveInventory("order-789"))
                .thenReturn("Custom inventory result");

        InventoryWorkflow workflow = client.newWorkflowStub(
                InventoryWorkflow.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );

        String result = workflow.reserveInventory("order-789");

        assertEquals("Custom inventory result", result);
    }
}
