package com.example.rabbitmqpoc.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class RabbitMQConfig {

    @Bean
    MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter(JsonMapper.builder().build());
    }

    @Bean
    RabbitTemplate rabbitTemplate(CachingConnectionFactory connectionFactory,
                                 MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);

        // Publisher Confirms
        connectionFactory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.CORRELATED);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                System.err.printf("[Confirm] NACK! cause=%s, correlationData=%s%n", cause, correlationData);
            }
        });

        // Return Callback (mandatory message that cannot be routed)
        template.setReturnsCallback(returned ->
            System.err.printf("[Return] Message returned: exchange=%s, routingKey=%s, replyText=%s%n",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyText())
        );
        template.setMandatory(true);

        return template;
    }
}
