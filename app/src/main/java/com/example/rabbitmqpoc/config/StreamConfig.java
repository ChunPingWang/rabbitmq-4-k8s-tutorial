package com.example.rabbitmqpoc.config;

import com.rabbitmq.stream.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.rabbit.stream.producer.RabbitStreamTemplate;

@Configuration
public class StreamConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host:poc}")
    private String virtualHost;

    @Bean
    Environment rabbitStreamEnvironment() {
        return Environment.builder()
            .host(host)
            .port(5552)
            .username(username)
            .password(password)
            .virtualHost(virtualHost)
            .build();
    }

    @Bean
    RabbitStreamTemplate rabbitStreamTemplate(Environment env) {
        return new RabbitStreamTemplate(env, "audit.stream");
    }
}
