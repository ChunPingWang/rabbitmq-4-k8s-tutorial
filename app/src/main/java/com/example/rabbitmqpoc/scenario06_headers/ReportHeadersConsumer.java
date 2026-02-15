package com.example.rabbitmqpoc.scenario06_headers;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ReportHeadersConsumer {

    @RabbitListener(queues = "report.pdf")
    public void handlePdf(Message message, Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Headers:PDF] Generating PDF report: %s%n", new String(message.getBody()));
        channel.basicAck(tag, false);
    }

    @RabbitListener(queues = "report.excel")
    public void handleExcel(Message message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        System.out.printf("[Headers:Excel] Generating Excel report: %s%n", new String(message.getBody()));
        channel.basicAck(tag, false);
    }
}
