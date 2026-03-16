package com.nova.temporal_playground.controller;

import com.nova.temporal_playground.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ApprovalController {

    private final WorkflowClient workflowClient;

    public ApprovalController(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @PostMapping("/orders/{id}/approve")
    public ResponseEntity<String> approveOrder(@PathVariable String id) {
        try {
            OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, "order-" + id);
            workflow.signalApproval(true);
            log.info("Approval signal sent for order: {}", id);
            return ResponseEntity.ok("Order " + id + " approved");
        } catch (Exception e) {
            log.error("Failed to send approval signal for order: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Workflow not found for order: " + id);
        }
    }

    @PostMapping("/orders/{id}/reject")
    public ResponseEntity<String> rejectOrder(@PathVariable String id) {
        try {
            OrderWorkflow workflow = workflowClient.newWorkflowStub(OrderWorkflow.class, "order-" + id);
            workflow.signalApproval(false);
            log.info("Rejection signal sent for order: {}", id);
            return ResponseEntity.ok("Order " + id + " rejected");
        } catch (Exception e) {
            log.error("Failed to send rejection signal for order: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Workflow not found for order: " + id);
        }
    }
}
