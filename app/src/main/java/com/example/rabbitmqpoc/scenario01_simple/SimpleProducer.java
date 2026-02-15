package com.example.rabbitmqpoc.scenario01_simple;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class SimpleProducer {

    private final RabbitTemplate rabbitTemplate;

    public SimpleProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(String message) {
        rabbitTemplate.convertAndSend("simple.queue", message);
        System.out.println("[Simple] Sent: " + message);
    }
}
