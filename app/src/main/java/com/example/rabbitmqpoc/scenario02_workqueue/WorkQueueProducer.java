package com.example.rabbitmqpoc.scenario02_workqueue;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class WorkQueueProducer {

    private final RabbitTemplate rabbitTemplate;

    public WorkQueueProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendTasks(int count) {
        for (int i = 1; i <= count; i++) {
            String task = "Task-" + i;
            rabbitTemplate.convertAndSend("task.queue", task);
            System.out.println("[WorkQueue] Sent: " + task);
        }
    }
}
