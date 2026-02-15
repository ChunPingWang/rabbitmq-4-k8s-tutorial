package com.example.rabbitmqpoc.scenario08_dlx;

import com.example.rabbitmqpoc.model.OrderMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class DlxProducer {

    private final RabbitTemplate rabbitTemplate;

    public DlxProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPoisonMessage() {
        OrderMessage poison = new OrderMessage(
            "POISON-001", "BAD-CUST", BigDecimal.valueOf(-1), "TWD", null);
        rabbitTemplate.convertAndSend("order.direct", "order.created", poison);
        System.out.println("[DLX] Sent poison message (negative amount) → will be rejected → DLX");
    }
}
