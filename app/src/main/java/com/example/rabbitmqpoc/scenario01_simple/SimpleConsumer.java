package com.example.rabbitmqpoc.scenario01_simple;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class SimpleConsumer {

    @RabbitListener(queues = "simple.queue")
    public void receive(String message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.println("[Simple] Received: " + message);
        channel.basicAck(tag, false);
    }
}
