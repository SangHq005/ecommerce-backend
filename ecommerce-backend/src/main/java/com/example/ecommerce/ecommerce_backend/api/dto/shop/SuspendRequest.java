package com.example.ecommerce.ecommerce_backend.api.dto.shop;

import jakarta.validation.constraints.NotBlank;

public record SuspendRequest(@NotBlank String reason) {}
