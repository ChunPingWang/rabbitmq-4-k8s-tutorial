package com.example.rabbitmqpoc.model;

import java.time.LocalDateTime;

public record NotificationMessage(
    String title,
    String body,
    String recipient,
    String channel,
    LocalDateTime sentAt
) {
    public NotificationMessage {
        if (sentAt == null) sentAt = LocalDateTime.now();
    }
}
