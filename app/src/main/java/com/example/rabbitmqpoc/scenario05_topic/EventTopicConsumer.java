package com.example.rabbitmqpoc.scenario05_topic;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EventTopicConsumer {

    @RabbitListener(queues = "event.all-orders")
    public void handleAllOrders(String message, Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("[Topic:order.#] " + message);
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "event.payments")
    public void handlePayments(String message, Channel channel,
                               @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("[Topic:payment.*] " + message);
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "event.audit")
    public void handleAudit(String message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("[Topic:#(audit)] " + message);
        channel.basicAck(tag, false);
    }
}
