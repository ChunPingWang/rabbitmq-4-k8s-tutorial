package com.example.rabbitmqpoc.scenario04_routing;

import com.example.rabbitmqpoc.model.OrderMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderRoutingProducer {

    private final RabbitTemplate rabbitTemplate;

    public OrderRoutingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendOrderEvent(OrderMessage order, String status) {
        String routingKey = "order." + status;
        rabbitTemplate.convertAndSend("order.direct", routingKey, order);
        System.out.printf("[Routing] Sent order %s with key=%s%n", order.orderId(), routingKey);
    }
}
