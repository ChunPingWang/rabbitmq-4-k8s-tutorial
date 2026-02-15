package com.example.rabbitmqpoc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderMessage(
    String orderId,
    String customerId,
    BigDecimal amount,
    String currency,
    LocalDateTime createdAt
) {
    public OrderMessage {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (currency == null) currency = "TWD";
    }
}
