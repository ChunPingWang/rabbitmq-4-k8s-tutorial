package com.example.rabbitmqpoc.scenario08_dlx;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class DlxConsumer {

    @RabbitListener(queues = "dead.letter.queue")
    public void handleDeadLetter(Message message, Channel channel,
                                 @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[DLX] Dead letter received! body=%s, headers=%s%n",
            new String(message.getBody()),
            message.getMessageProperties().getHeaders());
        // ACK to prevent re-delivery loop; log for manual investigation
        channel.basicAck(tag, false);
    }
}
