package com.example.rabbitmqpoc.scenario07_rpc;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class RpcServer {

    @RabbitListener(queues = "rpc.credit-score")
    public String handleCreditScoreRequest(String customerId) {
        System.out.printf("[RPC-Server] Processing credit score for: %s%n", customerId);
        // Simulate credit score calculation
        int score = ThreadLocalRandom.current().nextInt(300, 851);
        String result = String.format("{\"customerId\":\"%s\",\"score\":%d,\"rating\":\"%s\"}",
            customerId, score, score >= 700 ? "EXCELLENT" : score >= 600 ? "GOOD" : "FAIR");
        System.out.printf("[RPC-Server] Returning: %s%n", result);
        return result;
    }
}
