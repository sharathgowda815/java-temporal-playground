package com.nova.temporal_playground.workflow.payment;

import com.nova.temporal_playground.activity.PaymentActivities;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import org.slf4j.Logger;

import java.time.Duration;

public class PaymentWorkflowImpl implements PaymentWorkflow {

    private static final Logger log = Workflow.getLogger(PaymentWorkflowImpl.class);

    private final PaymentActivities activities = Workflow.newActivityStub(
            PaymentActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(RetryOptions.newBuilder()
                            .setMaximumAttempts(3)
                            .setInitialInterval(Duration.ofSeconds(2))
                            .setBackoffCoefficient(2.0)
                            .build())
                    .build()
    );

    @Override
    public String processPayment(String orderId) {
        log.info("PaymentWorkflow started for order: {}", orderId);
        String result = activities.processPayment(orderId);
        log.info("PaymentWorkflow completed for order: {}", orderId);
        return result;
    }
}
