package com.example.rabbitmqpoc.scenario05_topic;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class EventTopicProducer {

    private final RabbitTemplate rabbitTemplate;

    public EventTopicProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishEvent(String routingKey, String data) {
        rabbitTemplate.convertAndSend("event.topic", routingKey, data);
        System.out.printf("[Topic] Published: key=%s, data=%s%n", routingKey, data);
    }
}
