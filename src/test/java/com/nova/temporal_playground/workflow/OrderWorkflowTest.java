package com.nova.temporal_playground.workflow;

import com.nova.temporal_playground.activity.InventoryActivities;
import com.nova.temporal_playground.activity.OrderActivities;
import com.nova.temporal_playground.activity.PaymentActivities;
import com.nova.temporal_playground.activity.ShippingActivities;
import com.nova.temporal_playground.model.OrderEvent;
import com.nova.temporal_playground.workflow.inventory.InventoryWorkflowImpl;
import com.nova.temporal_playground.workflow.payment.PaymentWorkflowImpl;
import com.nova.temporal_playground.workflow.shipping.ShippingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderWorkflowTest {

    private static final String TASK_QUEUE = "order-test-queue";

    private TestWorkflowEnvironment testEnv;
    private WorkflowClient client;

    private OrderActivities orderActivities;
    private PaymentActivities paymentActivities;
    private InventoryActivities inventoryActivities;
    private ShippingActivities shippingActivities;

    @BeforeEach
    void setUp() {
        testEnv = TestWorkflowEnvironment.newInstance();

        orderActivities = mock(OrderActivities.class, withSettings().withoutAnnotations());
        paymentActivities = mock(PaymentActivities.class, withSettings().withoutAnnotations());
        inventoryActivities = mock(InventoryActivities.class, withSettings().withoutAnnotations());
        shippingActivities = mock(ShippingActivities.class, withSettings().withoutAnnotations());

        Worker worker = testEnv.newWorker(TASK_QUEUE);
        worker.registerWorkflowImplementationTypes(
                OrderWorkflowImpl.class,
                PaymentWorkflowImpl.class,
                InventoryWorkflowImpl.class,
                ShippingWorkflowImpl.class
        );
        worker.registerActivitiesImplementations(
                orderActivities,
                paymentActivities,
                inventoryActivities,
                shippingActivities
        );
        testEnv.start();
        client = testEnv.getWorkflowClient();
    }

    @AfterEach
    void tearDown() {
        testEnv.close();
    }

    @Test
    void processOrder_approved_shouldCompleteSuccessfully()
            throws ExecutionException, InterruptedException, TimeoutException {
        when(paymentActivities.processPayment("order-123"))
                .thenReturn("Payment completed");
        when(inventoryActivities.reserveInventory("order-123"))
                .thenReturn("Inventory reserved");
        when(shippingActivities.shipOrder("order-123"))
                .thenReturn("Order shipped");

        OrderWorkflow workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("order-order-123")
                        .build()
        );

        OrderEvent event = new OrderEvent("order-123", "customer-1", new BigDecimal("100.00"));
        CompletableFuture<String> resultFuture = WorkflowClient.execute(workflow::processOrder, event);

        OrderWorkflow signalStub = client.newWorkflowStub(OrderWorkflow.class, "order-order-123");
        signalStub.signalApproval(true);

        String result = resultFuture.get(30, TimeUnit.SECONDS);

        assertEquals("Order order-123 processed successfully", result);
        verify(orderActivities).validateOrder("order-123");
        verify(paymentActivities).processPayment("order-123");
        verify(inventoryActivities).reserveInventory("order-123");
        verify(shippingActivities).shipOrder("order-123");
        verify(orderActivities).sendConfirmation("order-123");
    }

    @Test
    void processOrder_rejected_shouldReturnRejectionMessageAndCompensate()
            throws ExecutionException, InterruptedException, TimeoutException {
        when(paymentActivities.processPayment("order-456"))
                .thenReturn("Payment completed");
        when(inventoryActivities.reserveInventory("order-456"))
                .thenReturn("Inventory reserved");

        OrderWorkflow workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("order-order-456")
                        .build()
        );

        OrderEvent event = new OrderEvent("order-456", "customer-2", new BigDecimal("200.00"));
        CompletableFuture<String> resultFuture = WorkflowClient.execute(workflow::processOrder, event);

        OrderWorkflow signalStub = client.newWorkflowStub(OrderWorkflow.class, "order-order-456");
        signalStub.signalApproval(false);

        String result = resultFuture.get(30, TimeUnit.SECONDS);

        assertEquals("Order order-456 was rejected", result);
        verify(orderActivities).validateOrder("order-456");
        verify(paymentActivities).processPayment("order-456");
        verify(inventoryActivities).reserveInventory("order-456");
        verify(shippingActivities, never()).shipOrder(anyString());
        verify(orderActivities, never()).sendConfirmation(anyString());
        // Verify compensation activities are called
        verify(paymentActivities).refundPayment("order-456");
        verify(inventoryActivities).releaseInventory("order-456");
    }

    @Test
    void processOrder_approved_shouldCallActivitiesInCorrectOrder()
            throws ExecutionException, InterruptedException, TimeoutException {
        when(paymentActivities.processPayment("order-789"))
                .thenReturn("Payment done");
        when(inventoryActivities.reserveInventory("order-789"))
                .thenReturn("Inventory done");
        when(shippingActivities.shipOrder("order-789"))
                .thenReturn("Shipped");

        OrderWorkflow workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("order-order-789")
                        .build()
        );

        OrderEvent event = new OrderEvent("order-789", "customer-3", new BigDecimal("300.00"));
        CompletableFuture<String> resultFuture = WorkflowClient.execute(workflow::processOrder, event);

        OrderWorkflow signalStub = client.newWorkflowStub(OrderWorkflow.class, "order-order-789");
        signalStub.signalApproval(true);

        resultFuture.get(30, TimeUnit.SECONDS);

        var inOrder = inOrder(orderActivities, shippingActivities);
        inOrder.verify(orderActivities).validateOrder("order-789");
        inOrder.verify(shippingActivities).shipOrder("order-789");
        inOrder.verify(orderActivities).sendConfirmation("order-789");
    }

    @Test
    void processOrder_parallelExecution_paymentAndInventoryBothCalled()
            throws ExecutionException, InterruptedException, TimeoutException {
        when(paymentActivities.processPayment("order-parallel"))
                .thenReturn("Payment completed");
        when(inventoryActivities.reserveInventory("order-parallel"))
                .thenReturn("Inventory reserved");
        when(shippingActivities.shipOrder("order-parallel"))
                .thenReturn("Shipped");

        OrderWorkflow workflow = client.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TASK_QUEUE)
                        .setWorkflowId("order-order-parallel")
                        .build()
        );

        OrderEvent event = new OrderEvent("order-parallel", "customer-4", new BigDecimal("400.00"));
        CompletableFuture<String> resultFuture = WorkflowClient.execute(workflow::processOrder, event);

        OrderWorkflow signalStub = client.newWorkflowStub(OrderWorkflow.class, "order-order-parallel");
        signalStub.signalApproval(true);

        resultFuture.get(30, TimeUnit.SECONDS);

        verify(paymentActivities).processPayment("order-parallel");
        verify(inventoryActivities).reserveInventory("order-parallel");
    }
}
