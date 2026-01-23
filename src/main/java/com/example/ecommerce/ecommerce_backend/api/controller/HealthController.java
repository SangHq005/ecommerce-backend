package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.Map;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.filter.CorrelationIdFilter;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Value("${spring.application.name:ecommerce-backend}")
    private String appName;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the application is running")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> data = Map.of(
                "status", "UP",
                "name", appName,
                "correlationId", MDC.get(CorrelationIdFilter.MDC_KEY)
        );
        return ResponseHelper.ok(data);
    }

    @GetMapping("/api/v1/_debug/ping")
    @Operation(summary = "Ping", description = "Debug ping endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ping() {
        Map<String, Object> data = Map.of(
                "pong", true,
                "correlationId", MDC.get(CorrelationIdFilter.MDC_KEY)
        );
        return ResponseHelper.ok(data);
    }
}
