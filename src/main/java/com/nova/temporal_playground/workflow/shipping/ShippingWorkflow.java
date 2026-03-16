package com.nova.temporal_playground.workflow.shipping;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface ShippingWorkflow {

    @WorkflowMethod
    String shipOrder(String orderId);
}
