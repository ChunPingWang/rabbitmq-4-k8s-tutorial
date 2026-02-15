package com.example.rabbitmqpoc.config;

import com.rabbitmq.stream.Environment;
import com.rabbitmq.stream.EnvironmentBuilder;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
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

    @Value("${spring.rabbitmq.ssl.enabled:false}")
    private boolean sslEnabled;

    @Bean
    @Lazy
    Environment rabbitStreamEnvironment() throws Exception {
        EnvironmentBuilder builder = Environment.builder()
            .host(host)
            .port(5552)
            .username(username)
            .password(password)
            .virtualHost(virtualHost);

        if (sslEnabled) {
            builder.tls()
                .sslContext(SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build())
                .environmentBuilder();
        }

        return builder.build();
    }

    @Bean
    @Lazy
    RabbitStreamTemplate rabbitStreamTemplate(Environment env) {
        return new RabbitStreamTemplate(env, "audit.stream");
    }
}
