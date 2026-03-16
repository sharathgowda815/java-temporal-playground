package com.nova.temporal_playground.workflow;

import com.nova.temporal_playground.activity.InventoryActivities;
import com.nova.temporal_playground.activity.OrderActivities;
import com.nova.temporal_playground.activity.PaymentActivities;
import com.nova.temporal_playground.model.OrderEvent;
import com.nova.temporal_playground.workflow.inventory.InventoryWorkflow;
import com.nova.temporal_playground.workflow.payment.PaymentWorkflow;
import com.nova.temporal_playground.workflow.shipping.ShippingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Async;
import io.temporal.workflow.ChildWorkflowOptions;
import io.temporal.workflow.Promise;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class OrderWorkflowImpl implements OrderWorkflow {

    private static final Logger log = Workflow.getLogger(OrderWorkflowImpl.class);

    private static final RetryOptions DEFAULT_RETRY = RetryOptions.newBuilder()
            .setMaximumAttempts(3)
            .setInitialInterval(Duration.ofSeconds(1))
            .setBackoffCoefficient(2.0)
            .build();

    private final OrderActivities orderActivities = Workflow.newActivityStub(
            OrderActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(DEFAULT_RETRY)
                    .build()
    );

    private final PaymentActivities paymentActivities = Workflow.newActivityStub(
            PaymentActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(DEFAULT_RETRY)
                    .build()
    );

    private final InventoryActivities inventoryActivities = Workflow.newActivityStub(
            InventoryActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(DEFAULT_RETRY)
                    .build()
    );

    private boolean approved = false;
    private boolean approvalReceived = false;

    @Override
    public String processOrder(OrderEvent orderEvent) {
        String orderId = orderEvent.getOrderId();
        log.info("Parent OrderWorkflow started for order: {}, customer: {}, amount: {}",
                orderId, orderEvent.getCustomerId(), orderEvent.getAmount());

        // Step 1: Validate order
        orderActivities.validateOrder(orderId);

        // Step 2 & 3: Start Payment and Inventory child workflows in parallel
        PaymentWorkflow paymentWorkflow = Workflow.newChildWorkflowStub(
                PaymentWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                        .setWorkflowId("payment-" + orderId)
                        .build()
        );

        InventoryWorkflow inventoryWorkflow = Workflow.newChildWorkflowStub(
                InventoryWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                        .setWorkflowId("inventory-" + orderId)
                        .build()
        );

        log.info("Starting Payment and Inventory child workflows in parallel for order: {}", orderId);
        Promise<String> paymentPromise = Async.function(paymentWorkflow::processPayment, orderId);
        Promise<String> inventoryPromise = Async.function(inventoryWorkflow::reserveInventory, orderId);

        // Wait for both to complete
        String paymentResult = paymentPromise.get();
        String inventoryResult = inventoryPromise.get();
        log.info("Payment result: {} | Inventory result: {}", paymentResult, inventoryResult);

        // Step 4: Wait for approval signal (with 24h timeout)
        log.info("Waiting for approval signal for order: {}", orderId);
        boolean received = Workflow.await(Duration.ofHours(24), () -> approvalReceived);

        if (!received) {
            log.info("Order {} timed out waiting for approval, starting compensation", orderId);
            compensate(orderId);
            return "Order " + orderId + " timed out waiting for approval";
        }

        if (!approved) {
            log.info("Order {} was rejected, starting compensation", orderId);
            compensate(orderId);
            return "Order " + orderId + " was rejected";
        }

        // Step 5: Start Shipping child workflow
        ShippingWorkflow shippingWorkflow = Workflow.newChildWorkflowStub(
                ShippingWorkflow.class,
                ChildWorkflowOptions.newBuilder()
                        .setWorkflowId("shipping-" + orderId)
                        .build()
        );

        log.info("Starting Shipping child workflow for order: {}", orderId);
        String shippingResult = shippingWorkflow.shipOrder(orderId);
        log.info("Shipping result: {}", shippingResult);

        // Step 6: Send confirmation
        orderActivities.sendConfirmation(orderId);

        log.info("Parent OrderWorkflow completed for order: {}", orderId);
        return "Order " + orderId + " processed successfully";
    }

    @Override
    public void signalApproval(boolean approved) {
        this.approved = approved;
        this.approvalReceived = true;
    }

    private void compensate(String orderId) {
        paymentActivities.refundPayment(orderId);
        inventoryActivities.releaseInventory(orderId);
    }
}
