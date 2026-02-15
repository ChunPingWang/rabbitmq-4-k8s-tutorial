package com.example.rabbitmqpoc.scenario06_headers;

import com.example.rabbitmqpoc.model.ReportRequest;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Service;

@Service
public class ReportHeadersProducer {

    private final RabbitTemplate rabbitTemplate;
    private final MessageConverter messageConverter;

    public ReportHeadersProducer(RabbitTemplate rabbitTemplate, MessageConverter messageConverter) {
        this.rabbitTemplate = rabbitTemplate;
        this.messageConverter = messageConverter;
    }

    public void requestReport(ReportRequest request, String format, String department) {
        MessageProperties props = new MessageProperties();
        props.setHeader("format", format);
        props.setHeader("department", department);

        Message message = messageConverter.toMessage(request, props);
        rabbitTemplate.send("report.headers", "", message);
        System.out.printf("[Headers] Report requested: format=%s, dept=%s%n", format, department);
    }
}
