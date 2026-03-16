package com.nova.temporal_playground.integration;

import com.nova.temporal_playground.config.KafkaConfig;
import com.nova.temporal_playground.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@AutoConfigureMockMvc
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaConfig.ORDER_EVENTS_TOPIC},
        brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"}
)
class OrderProcessingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowServiceStubs workflowServiceStubs;

    @MockitoBean
    private WorkflowClient workflowClient;

    @MockitoBean
    private WorkerFactory workerFactory;

    @MockitoBean
    private Worker worker;

    @Test
    void createOrder_shouldPublishToKafkaAndReturn200() throws Exception {
        String json = """
                {"orderId": "integ-001", "customerId": "cust-integ", "amount": 500.0}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Order event published for order: integ-001"));
    }

    @Test
    void approveOrder_shouldSendSignalToWorkflow() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-integ-002"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/integ-002/approve"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order integ-002 approved"));

        verify(mockWorkflow).signalApproval(true);
    }

    @Test
    void rejectOrder_shouldSendRejectionSignalToWorkflow() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-integ-003"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/integ-003/reject"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order integ-003 rejected"));

        verify(mockWorkflow).signalApproval(false);
    }

    @Test
    void kafkaConsumer_shouldStartWorkflowWhenEventReceived() throws Exception {
        // Set up mock to capture the workflow stub creation
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class)))
                .thenReturn(mockWorkflow);

        String json = """
                {"orderId": "integ-004", "customerId": "cust-kafka", "amount": 750.0}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // Allow time for Kafka consumer to process the event
        Thread.sleep(5000);

        // Verify the consumer created a workflow stub and started the workflow
        verify(workflowClient, atLeastOnce()).newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class));
    }

    @Test
    void fullFlow_publishAndConsume_shouldCreateWorkflow() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class)))
                .thenReturn(mockWorkflow);

        String json = """
                {"orderId": "integ-005", "customerId": "cust-e2e", "amount": 999.99}
                """;

        // Publish via REST endpoint
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Order event published for order: integ-005"));

        // Wait for Kafka consumer to pick it up
        Thread.sleep(5000);

        // Verify the consumer received the event and started a workflow
        verify(workflowClient, atLeastOnce()).newWorkflowStub(eq(OrderWorkflow.class), any(WorkflowOptions.class));
    }
}
