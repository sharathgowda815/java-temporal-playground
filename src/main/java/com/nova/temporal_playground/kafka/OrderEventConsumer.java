package com.nova.temporal_playground.kafka;

import com.nova.temporal_playground.config.KafkaConfig;
import com.nova.temporal_playground.config.TemporalConfig;
import com.nova.temporal_playground.model.OrderEvent;
import com.nova.temporal_playground.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventConsumer {

    private final WorkflowClient workflowClient;

    public OrderEventConsumer(WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    @KafkaListener(topics = KafkaConfig.ORDER_EVENTS_TOPIC, groupId = "order-group")
    public void consume(OrderEvent event) {
        log.info("Received order event from Kafka: orderId={}, customerId={}, amount={}",
                event.getOrderId(), event.getCustomerId(), event.getAmount());

        OrderWorkflow workflow = workflowClient.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue(TemporalConfig.ORDER_TASK_QUEUE)
                        .setWorkflowId("order-" + event.getOrderId())
                        .build()
        );

        try {
            WorkflowClient.start(workflow::processOrder, event);
            log.info("Started Temporal workflow for order: {}", event.getOrderId());
        } catch (WorkflowExecutionAlreadyStarted e) {
            log.warn("Workflow already running for order: {}", event.getOrderId());
        }
    }
}
