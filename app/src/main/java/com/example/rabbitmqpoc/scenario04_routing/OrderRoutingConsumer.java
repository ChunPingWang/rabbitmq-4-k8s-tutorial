package com.example.rabbitmqpoc.scenario04_routing;

import com.example.rabbitmqpoc.model.OrderMessage;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class OrderRoutingConsumer {

    @RabbitListener(queues = "order.created")
    public void handleCreated(OrderMessage order, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Routing] Order CREATED: %s, amount=%s%n", order.orderId(), order.amount());
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "order.paid")
    public void handlePaid(OrderMessage order, Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Routing] Order PAID: %s, amount=%s%n", order.orderId(), order.amount());
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "order.shipped")
    public void handleShipped(OrderMessage order, Channel channel,
                              @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Routing] Order SHIPPED: %s%n", order.orderId());
        channel.basicAck(tag, false);
    }
}
