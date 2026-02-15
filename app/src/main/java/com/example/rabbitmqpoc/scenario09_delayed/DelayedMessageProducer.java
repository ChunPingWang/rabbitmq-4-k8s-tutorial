package com.example.rabbitmqpoc.scenario09_delayed;

import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class DelayedMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public DelayedMessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Send message to the delay parking queue with custom TTL.
     * When TTL expires, the message is forwarded via DLX to order.created queue.
     */
    public void sendWithCustomDelay(String message, long delayMs) {
        MessagePostProcessor postProcessor = msg -> {
            msg.getMessageProperties().setExpiration(String.valueOf(delayMs));
            return msg;
        };
        rabbitTemplate.convertAndSend("delay.parking", message, postProcessor);
        System.out.printf("[Delayed] Sent with delay=%dms: %s%n", delayMs, message);
    }

    public void sendWithDefaultDelay(String message) {
        // Uses the queue-level TTL (30s) defined in 08-queues.yaml
        rabbitTemplate.convertAndSend("delay.parking", message);
        System.out.printf("[Delayed] Sent with default delay (30s): %s%n", message);
    }
}
