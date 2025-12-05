package com.example.ecommerce.ecommerce_backend.api.dto.shop;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShopUpsertRequest(
        @NotBlank @Size(max=191) String shopName,
        @Size(max=2000) String description
) {}
