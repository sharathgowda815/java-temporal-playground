package com.nova.temporal_playground.workflow.shipping;

import com.nova.temporal_playground.activity.ShippingActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class ShippingWorkflowImpl implements ShippingWorkflow {

    private static final Logger log = Workflow.getLogger(ShippingWorkflowImpl.class);

    private final ShippingActivities activities = Workflow.newActivityStub(
            ShippingActivities.class,
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
    public String shipOrder(String orderId) {
        log.info("ShippingWorkflow started for order: {}", orderId);
        String result = activities.shipOrder(orderId);
        log.info("ShippingWorkflow completed for order: {}", orderId);
        return result;
    }
}
