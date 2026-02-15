package com.example.rabbitmqpoc.scenario10_priority;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class PriorityProducer {

    private final RabbitTemplate rabbitTemplate;

    public PriorityProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendWithPriority(String message, int priority) {
        rabbitTemplate.convertAndSend("priority.queue", message, msg -> {
            msg.getMessageProperties().setPriority(priority);
            return msg;
        });
        System.out.printf("[Priority] Sent (priority=%d): %s%n", priority, message);
    }
}
