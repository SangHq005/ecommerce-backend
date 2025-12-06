package com.example.ecommerce.ecommerce_backend.api.dto.seller;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status,

        String note,
        String trackingNumber
) {
}
