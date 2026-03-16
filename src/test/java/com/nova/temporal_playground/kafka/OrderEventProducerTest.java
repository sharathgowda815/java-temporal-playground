package com.nova.temporal_playground.kafka;

import com.nova.temporal_playground.config.KafkaConfig;
import com.nova.temporal_playground.model.OrderEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    private OrderEventProducer producer;

    @BeforeEach
    void setUp() {
        when(kafkaTemplate.send(anyString(), anyString(), any(OrderEvent.class)))
                .thenReturn(new CompletableFuture<>());
        producer = new OrderEventProducer(kafkaTemplate);
    }

    @Test
    void sendOrderEvent_shouldPublishToCorrectTopic() {
        OrderEvent event = new OrderEvent("order-1", "customer-1", new BigDecimal("100.00"));

        producer.sendOrderEvent(event);

        verify(kafkaTemplate).send(KafkaConfig.ORDER_EVENTS_TOPIC, "order-1", event);
    }

    @Test
    void sendOrderEvent_shouldUseOrderIdAsKey() {
        OrderEvent event = new OrderEvent("order-xyz", "customer-2", new BigDecimal("250.00"));

        producer.sendOrderEvent(event);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

        assertEquals(KafkaConfig.ORDER_EVENTS_TOPIC, topicCaptor.getValue());
        assertEquals("order-xyz", keyCaptor.getValue());
        assertEquals(event, eventCaptor.getValue());
    }
}
