package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.filter.CorrelationIdFilter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @Value("${spring.application.name:ecommerce-backend}")
    private String appName;

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "UP",
                "name", appName,
                "correlationId", MDC.get(CorrelationIdFilter.MDC_KEY)
        );
    }

    @GetMapping("/api/v1/_debug/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "pong", true,
                "correlationId", MDC.get(CorrelationIdFilter.MDC_KEY)
        );
    }
}
