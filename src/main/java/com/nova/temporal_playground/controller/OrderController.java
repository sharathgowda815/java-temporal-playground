package com.nova.temporal_playground.controller;

import com.nova.temporal_playground.kafka.OrderEventProducer;
import com.nova.temporal_playground.model.OrderEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderEventProducer orderEventProducer;

    public OrderController(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    @PostMapping("/orders")
    public ResponseEntity<String> createOrder(@RequestBody OrderEvent event) {
        if (event.getOrderId() == null || event.getOrderId().isBlank()) {
            return ResponseEntity.badRequest().body("orderId is required");
        }
        orderEventProducer.sendOrderEvent(event);
        return ResponseEntity.ok("Order event published for order: " + event.getOrderId());
    }
}
