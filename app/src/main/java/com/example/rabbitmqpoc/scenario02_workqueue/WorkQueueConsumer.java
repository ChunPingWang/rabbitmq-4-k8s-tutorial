package com.example.rabbitmqpoc.scenario02_workqueue;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class WorkQueueConsumer {

    @RabbitListener(queues = "task.queue", concurrency = "2-5")
    public void receive(String task,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[WorkQueue] Processing: %s (thread=%s)%n",
            task, Thread.currentThread().getName());
        try {
            // Simulate work
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        channel.basicAck(tag, false);
        System.out.printf("[WorkQueue] Done: %s%n", task);
    }
}
