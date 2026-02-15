package com.example.rabbitmqpoc.scenario12_stream;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@org.springframework.context.annotation.Lazy
public class StreamConsumer {

    /**
     * Stream Consumer can start from first, last, or a specific offset.
     * Multiple consumers reading the same stream don't compete (non-competing consumer).
     * Suitable for event sourcing and audit log scenarios.
     *
     * Uses @RabbitListener with containerFactory="rabbitStreamListenerContainerFactory"
     * which is auto-configured by Spring Boot when spring-rabbit-stream is on the classpath.
     */
    @RabbitListener(queues = "audit.stream", containerFactory = "rabbitListenerContainerFactory")
    public void handleStreamMessage(String message) {
        System.out.println("[Stream-Consumer] Received: " + message);
    }
}
