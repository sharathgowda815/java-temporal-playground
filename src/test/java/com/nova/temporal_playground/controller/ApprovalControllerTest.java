package com.nova.temporal_playground.controller;

import com.nova.temporal_playground.config.SecurityConfig;
import com.nova.temporal_playground.workflow.OrderWorkflow;
import io.temporal.client.WorkflowClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApprovalController.class)
@Import(SecurityConfig.class)
class ApprovalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkflowClient workflowClient;

    @Test
    void approveOrder_shouldSendApprovalSignal() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-123"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/123/approve"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order 123 approved"));

        verify(mockWorkflow).signalApproval(true);
    }

    @Test
    void rejectOrder_shouldSendRejectionSignal() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-456"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/456/reject"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order 456 rejected"));

        verify(mockWorkflow).signalApproval(false);
    }

    @Test
    void approveOrder_shouldUseCorrectWorkflowId() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-abc-xyz"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/abc-xyz/approve"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order abc-xyz approved"));

        verify(workflowClient).newWorkflowStub(OrderWorkflow.class, "order-abc-xyz");
    }

    @Test
    void rejectOrder_shouldUseCorrectWorkflowId() throws Exception {
        OrderWorkflow mockWorkflow = mock(OrderWorkflow.class);
        when(workflowClient.newWorkflowStub(OrderWorkflow.class, "order-999"))
                .thenReturn(mockWorkflow);

        mockMvc.perform(post("/orders/999/reject"))
                .andExpect(status().isOk())
                .andExpect(content().string("Order 999 rejected"));

        verify(workflowClient).newWorkflowStub(OrderWorkflow.class, "order-999");
    }
}
