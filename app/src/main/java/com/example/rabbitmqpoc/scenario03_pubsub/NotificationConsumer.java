package com.example.rabbitmqpoc.scenario03_pubsub;

import com.example.rabbitmqpoc.model.NotificationMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationConsumer {

    @RabbitListener(queues = "notification.email")
    public void handleEmail(NotificationMessage msg, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Email] Sending to %s: %s%n", msg.recipient(), msg.title());
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "notification.sms")
    public void handleSms(NotificationMessage msg, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[SMS] Sending to %s: %s%n", msg.recipient(), msg.title());
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "notification.push")
    public void handlePush(NotificationMessage msg, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Push] Sending to %s: %s%n", msg.recipient(), msg.title());
        channel.basicAck(tag, false);
    }
}
