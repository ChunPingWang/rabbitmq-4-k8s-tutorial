package com.example.rabbitmqpoc.scenario07_rpc;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RpcClient {

    private final RabbitTemplate rabbitTemplate;

    public RpcClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String callCreditScoreService(String customerId) {
        System.out.printf("[RPC-Client] Requesting credit score for: %s%n", customerId);
        Object response = rabbitTemplate.convertSendAndReceive(
            "rpc.exchange", "rpc.credit-score", customerId);
        String result = response != null ? response.toString() : "NO_RESPONSE";
        System.out.printf("[RPC-Client] Response: %s%n", result);
        return result;
    }
}
