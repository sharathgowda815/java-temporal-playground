package com.nova.temporal_playground.kafka;

import com.nova.temporal_playground.config.KafkaConfig;
import com.nova.temporal_playground.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventProducer {

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendOrderEvent(OrderEvent event) {
        log.info("Publishing order event to Kafka: {}", event.getOrderId());
        kafkaTemplate.send(KafkaConfig.ORDER_EVENTS_TOPIC, event.getOrderId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish order event for order: {}", event.getOrderId(), ex);
                    } else {
                        log.info("Successfully published order event for order: {} to partition: {}",
                                event.getOrderId(), result.getRecordMetadata().partition());
                    }
                });
    }
}
