package com.example.rabbitmqpoc.controller;

import com.example.rabbitmqpoc.model.*;
import com.example.rabbitmqpoc.scenario01_simple.SimpleProducer;
import com.example.rabbitmqpoc.scenario02_workqueue.WorkQueueProducer;
import com.example.rabbitmqpoc.scenario03_pubsub.NotificationProducer;
import com.example.rabbitmqpoc.scenario04_routing.OrderRoutingProducer;
import com.example.rabbitmqpoc.scenario05_topic.EventTopicProducer;
import com.example.rabbitmqpoc.scenario06_headers.ReportHeadersProducer;
import com.example.rabbitmqpoc.scenario07_rpc.RpcClient;
import com.example.rabbitmqpoc.scenario08_dlx.DlxProducer;
import com.example.rabbitmqpoc.scenario09_delayed.DelayedMessageProducer;
import com.example.rabbitmqpoc.scenario10_priority.PriorityProducer;
import com.example.rabbitmqpoc.scenario11_quorum.QuorumQueueDemo;
import com.example.rabbitmqpoc.scenario12_stream.StreamProducer;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    private final SimpleProducer simpleProducer;
    private final WorkQueueProducer workQueueProducer;
    private final NotificationProducer notificationProducer;
    private final OrderRoutingProducer orderRoutingProducer;
    private final EventTopicProducer eventTopicProducer;
    private final ReportHeadersProducer reportHeadersProducer;
    private final RpcClient rpcClient;
    private final DlxProducer dlxProducer;
    private final DelayedMessageProducer delayedMessageProducer;
    private final PriorityProducer priorityProducer;
    private final QuorumQueueDemo quorumQueueDemo;
    private final StreamProducer streamProducer;

    public DemoController(SimpleProducer simpleProducer,
                          WorkQueueProducer workQueueProducer,
                          NotificationProducer notificationProducer,
                          OrderRoutingProducer orderRoutingProducer,
                          EventTopicProducer eventTopicProducer,
                          ReportHeadersProducer reportHeadersProducer,
                          RpcClient rpcClient,
                          DlxProducer dlxProducer,
                          DelayedMessageProducer delayedMessageProducer,
                          PriorityProducer priorityProducer,
                          QuorumQueueDemo quorumQueueDemo,
                          StreamProducer streamProducer) {
        this.simpleProducer = simpleProducer;
        this.workQueueProducer = workQueueProducer;
        this.notificationProducer = notificationProducer;
        this.orderRoutingProducer = orderRoutingProducer;
        this.eventTopicProducer = eventTopicProducer;
        this.reportHeadersProducer = reportHeadersProducer;
        this.rpcClient = rpcClient;
        this.dlxProducer = dlxProducer;
        this.delayedMessageProducer = delayedMessageProducer;
        this.priorityProducer = priorityProducer;
        this.quorumQueueDemo = quorumQueueDemo;
        this.streamProducer = streamProducer;
    }

    /** 場景 1: Simple Queue */
    @PostMapping("/simple")
    public Map<String, String> simple(@RequestParam String msg) {
        simpleProducer.send(msg);
        return Map.of("status", "sent", "scenario", "Simple Queue");
    }

    /** 場景 2: Work Queue */
    @PostMapping("/work-queue")
    public Map<String, String> workQueue(@RequestParam(defaultValue = "10") int count) {
        workQueueProducer.sendTasks(count);
        return Map.of("status", "sent", "count", String.valueOf(count));
    }

    /** 場景 3: Pub/Sub */
    @PostMapping("/notification")
    public Map<String, String> notification(@RequestParam String title,
                                            @RequestParam String recipient) {
        notificationProducer.sendNotification(
            new NotificationMessage(title, "Body of " + title, recipient, "all", null));
        return Map.of("status", "broadcast", "scenario", "Fanout");
    }

    /** 場景 4: Routing */
    @PostMapping("/order/{status}")
    public Map<String, String> orderRouting(@PathVariable String status,
                                            @RequestParam(defaultValue = "ORD-001") String orderId) {
        orderRoutingProducer.sendOrderEvent(
            new OrderMessage(orderId, "CUST-001", BigDecimal.valueOf(1500), "TWD", null),
            status);
        return Map.of("status", "routed", "routing_key", "order." + status);
    }

    /** 場景 5: Topic */
    @PostMapping("/event")
    public Map<String, String> event(@RequestParam String routingKey,
                                     @RequestParam String data) {
        eventTopicProducer.publishEvent(routingKey, data);
        return Map.of("status", "published", "routing_key", routingKey);
    }

    /** 場景 6: Headers */
    @PostMapping("/report")
    public Map<String, String> report(@RequestParam String format,
                                      @RequestParam(defaultValue = "finance") String dept) {
        reportHeadersProducer.requestReport(
            new ReportRequest("Monthly Report", dept, LocalDate.now().minusMonths(1), LocalDate.now()),
            format, dept);
        return Map.of("status", "requested", "format", format, "department", dept);
    }

    /** 場景 7: RPC */
    @GetMapping("/credit-score/{customerId}")
    public Map<String, String> creditScore(@PathVariable String customerId) {
        String result = rpcClient.callCreditScoreService(customerId);
        return Map.of("result", result, "scenario", "RPC");
    }

    /** 場景 8: Dead Letter */
    @PostMapping("/poison")
    public Map<String, String> poison() {
        dlxProducer.sendPoisonMessage();
        return Map.of("status", "poison sent", "scenario", "DLX");
    }

    /** 場景 9: Delayed */
    @PostMapping("/delayed")
    public Map<String, String> delayed(@RequestParam String msg,
                                       @RequestParam(defaultValue = "30000") long delayMs) {
        delayedMessageProducer.sendWithCustomDelay(msg, delayMs);
        return Map.of("status", "delayed", "delay_ms", String.valueOf(delayMs));
    }

    /** 場景 10: Priority */
    @PostMapping("/priority")
    public Map<String, String> priority(@RequestParam String msg,
                                        @RequestParam(defaultValue = "5") int priority) {
        priorityProducer.sendWithPriority(msg, priority);
        return Map.of("status", "sent", "priority", String.valueOf(priority));
    }

    /** 場景 11: Quorum HA Test */
    @PostMapping("/ha-test")
    public Map<String, String> haTest() {
        quorumQueueDemo.demonstrateHighAvailability();
        return Map.of("status", "100 messages sent", "scenario", "Quorum HA");
    }

    /** 場景 12: Stream */
    @PostMapping("/stream")
    public Map<String, String> stream(@RequestParam String msg,
                                      @RequestParam(defaultValue = "1") int count) {
        if (count == 1) {
            streamProducer.sendToStream(msg);
        } else {
            streamProducer.sendBatch(count);
        }
        return Map.of("status", "sent", "count", String.valueOf(count), "scenario", "Stream");
    }
}
