package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminUserStatusRequest(
        @NotBlank(message = "Status is required")
        String status
) {}
