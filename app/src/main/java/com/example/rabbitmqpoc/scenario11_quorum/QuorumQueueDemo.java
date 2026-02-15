package com.example.rabbitmqpoc.scenario11_quorum;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class QuorumQueueDemo {

    private final RabbitTemplate rabbitTemplate;

    public QuorumQueueDemo(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Demonstrates Quorum Queue high availability.
     *
     * Test steps:
     * 1. Send 100 messages to a quorum queue
     * 2. Manually delete a RabbitMQ pod:
     *    kubectl delete pod rabbitmq-poc-cluster-server-1 -n rabbitmq-poc
     * 3. Messages should still be consumed normally (Raft consensus: 2/3 nodes alive)
     * 4. Pod auto-recovers; data syncs automatically
     */
    public void demonstrateHighAvailability() {
        for (int i = 1; i <= 100; i++) {
            String msg = "HA-Test-Message-" + i;
            rabbitTemplate.convertAndSend("task.queue", msg);
            if (i % 10 == 0) {
                System.out.println("[Quorum-HA] Sent " + i + " messages");
            }
        }
        System.out.println("[Quorum-HA] All 100 messages sent. "
            + "Now try: kubectl delete pod rabbitmq-poc-cluster-server-1");
    }
}
