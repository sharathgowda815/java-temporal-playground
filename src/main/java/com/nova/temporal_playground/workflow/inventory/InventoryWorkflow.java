package com.nova.temporal_playground.workflow.inventory;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface InventoryWorkflow {

    @WorkflowMethod
    String reserveInventory(String orderId);
}
