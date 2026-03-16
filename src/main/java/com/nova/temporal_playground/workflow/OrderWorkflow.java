package com.nova.temporal_playground.workflow;

import com.nova.temporal_playground.model.OrderEvent;
import io.temporal.workflow.SignalMethod;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface OrderWorkflow {

    @WorkflowMethod
    String processOrder(OrderEvent orderEvent);

    @SignalMethod
    void signalApproval(boolean approved);
}
