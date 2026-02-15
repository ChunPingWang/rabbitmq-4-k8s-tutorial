package com.example.rabbitmqpoc.scenario10_priority;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PriorityConsumer {

    @RabbitListener(queues = "priority.queue")
    public void receive(String message,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                        @Header(name = "amqp_receivedPriority", required = false)
                            Integer priority)
            throws IOException {
        System.out.printf("[Priority] Received (priority=%s): %s%n",
            priority != null ? priority : "default", message);
        channel.basicAck(tag, false);
    }
}
