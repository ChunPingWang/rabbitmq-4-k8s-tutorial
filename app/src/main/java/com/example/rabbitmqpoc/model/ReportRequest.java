package com.example.rabbitmqpoc.model;

import java.time.LocalDate;

public record ReportRequest(
    String reportName,
    String department,
    LocalDate startDate,
    LocalDate endDate
) {}
