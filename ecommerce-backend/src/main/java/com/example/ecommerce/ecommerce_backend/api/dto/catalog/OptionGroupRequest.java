package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OptionGroupRequest(
        @NotBlank String name,
        int sortOrder,
        @NotNull List<String> values
) {}
