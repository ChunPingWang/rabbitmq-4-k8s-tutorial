package com.example.rabbitmqpoc.scenario12_stream;

import org.springframework.rabbit.stream.listener.annotation.RabbitStreamListener;
import org.springframework.stereotype.Service;

@Service
public class StreamConsumer {

    /**
     * Stream Consumer can start from first, last, or a specific offset.
     * Multiple consumers reading the same stream don't compete (non-competing consumer).
     * Suitable for event sourcing and audit log scenarios.
     */
    @RabbitStreamListener(queues = "audit.stream")
    public void handleStreamMessage(String message) {
        System.out.println("[Stream-Consumer] Received: " + message);
    }
}
