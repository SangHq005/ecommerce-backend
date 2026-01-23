package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AdminUserRolesRequest(
        @NotNull(message = "Roles are required")
        List<String> roles
) {}
