package com.example.rabbitmqpoc.scenario12_stream;

import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;
import org.springframework.stereotype.Service;

@Service
@org.springframework.context.annotation.Lazy
public class StreamProducer {

    private final RabbitStreamTemplate streamTemplate;

    public StreamProducer(RabbitStreamTemplate streamTemplate) {
        this.streamTemplate = streamTemplate;
    }

    public void sendToStream(String message) {
        streamTemplate.convertAndSend(message);
        System.out.println("[Stream] Sent: " + message);
    }

    public void sendBatch(int count) {
        for (int i = 1; i <= count; i++) {
            streamTemplate.convertAndSend("Stream-Event-" + i);
        }
        System.out.printf("[Stream] Sent %d messages%n", count);
    }
}
