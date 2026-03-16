package com.nova.temporal_playground.controller;

import com.nova.temporal_playground.config.SecurityConfig;
import com.nova.temporal_playground.kafka.OrderEventProducer;
import com.nova.temporal_playground.model.OrderEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderEventProducer orderEventProducer;

    @Test
    void createOrder_shouldReturn200AndPublishEvent() throws Exception {
        String json = """
                {"orderId": "order-1", "customerId": "cust-1", "amount": 99.99}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Order event published for order: order-1"));

        ArgumentCaptor<OrderEvent> captor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(orderEventProducer).sendOrderEvent(captor.capture());
        OrderEvent captured = captor.getValue();
        assertEquals("order-1", captured.getOrderId());
        assertEquals("cust-1", captured.getCustomerId());
        assertEquals(new BigDecimal("99.99"), captured.getAmount());
    }

    @Test
    void createOrder_withDifferentPayload_shouldReturnCorrectMessage() throws Exception {
        String json = """
                {"orderId": "order-xyz", "customerId": "cust-2", "amount": 250.0}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Order event published for order: order-xyz"));

        verify(orderEventProducer).sendOrderEvent(
                org.mockito.ArgumentMatchers.argThat(event ->
                        "order-xyz".equals(event.getOrderId()) && "cust-2".equals(event.getCustomerId())
                )
        );
    }

    @Test
    void createOrder_withNullOrderId_shouldReturn400() throws Exception {
        String json = """
                {"orderId": null, "customerId": null, "amount": 0}
                """;

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("orderId is required"));
    }
}
