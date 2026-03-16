package com.nova.temporal_playground.workflow.inventory;

import com.nova.temporal_playground.activity.InventoryActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class InventoryWorkflowImpl implements InventoryWorkflow {

    private static final Logger log = Workflow.getLogger(InventoryWorkflowImpl.class);

    private final InventoryActivities activities = Workflow.newActivityStub(
            InventoryActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .setInitialInterval(Duration.ofSeconds(1))
                            .setBackoffCoefficient(2.0)
                            .build())
                    .build()
    );

    @Override
    public String reserveInventory(String orderId) {
        log.info("InventoryWorkflow started for order: {}", orderId);
        String result = activities.reserveInventory(orderId);
        log.info("InventoryWorkflow completed for order: {}", orderId);
        return result;
    }
}
