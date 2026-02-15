package com.example.rabbitmqpoc.scenario03_pubsub;

import com.example.rabbitmqpoc.model.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendNotification(NotificationMessage notification) {
        rabbitTemplate.convertAndSend("notification.fanout", "", notification);
        System.out.printf("[PubSub] Broadcast: %s to %s%n",
            notification.title(), notification.recipient());
    }
}
