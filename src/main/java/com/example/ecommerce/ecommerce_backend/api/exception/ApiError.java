package com.example.ecommerce.ecommerce_backend.api.exception;

import java.time.Instant;

public record ApiError(
        String code,
        String message,
        Instant timestamp,
        String correlationId
) {}
