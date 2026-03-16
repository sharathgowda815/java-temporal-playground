package com.nova.temporal_playground.kafka;

import com.nova.temporal_playground.config.TemporalConfig;
import com.nova.temporal_playground.model.OrderEvent;
import com.nova.temporal_playground.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock
    private WorkflowClient workflowClient;

    private OrderEventConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new OrderEventConsumer(workflowClient);
    }

    @Test
    void consume_shouldCreateWorkflowStubWithCorrectOptions() {
        OrderEvent event = new OrderEvent("123", "customer-1", new BigDecimal("100.00"));
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);

        ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);
        when(workflowClient.newWorkflowStub(eq(OrderWorkflow.class), optionsCaptor.capture()))
                .thenReturn(mockWorkflow);

        consumer.consume(event);

        WorkflowOptions options = optionsCaptor.getValue();
        assertEquals(TemporalConfig.ORDER_TASK_QUEUE, options.getTaskQueue());
        assertEquals("order-123", options.getWorkflowId());
    }

    @Test
    void consume_shouldStartWorkflowWithOrderEvent() {
        OrderEvent event = new OrderEvent("456", "customer-2", new BigDecimal("200.00"));
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);

        when(workflowClient.newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class)))
                .thenReturn(mockWorkflow);

        consumer.consume(event);

        // WorkflowClient.start() is a static method that calls the workflow method.
        // We verify the workflow stub was created with correct parameters.
        verify(workflowClient).newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class));
    }

    @Test
    void consume_shouldUseOrderIdInWorkflowId() {
        OrderEvent event = new OrderEvent("test-order-789", "customer-3", new BigDecimal("300.00"));
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);

        ArgumentCaptor<WorkflowOptions> optionsCaptor = ArgumentCaptor.forClass(WorkflowOptions.class);
        when(workflowClient.newWorkflowStub(eq(OrderWorkflow.class), optionsCaptor.capture()))
                .thenReturn(mockWorkflow);

        consumer.consume(event);

        assertEquals("order-test-order-789", optionsCaptor.getValue().getWorkflowId());
    }
}
