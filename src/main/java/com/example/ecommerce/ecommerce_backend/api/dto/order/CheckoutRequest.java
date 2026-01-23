package com.example.ecommerce.ecommerce_backend.api.dto.order;

import jakarta.validation.constraints.*;
import java.util.List;

public record CheckoutRequest(
        @NotEmpty(message = "Items must not be empty") List<Item> items,
        @NotNull(message = "Address ID is required") Long addressId,
        @NotBlank(message = "Payment method is required") String paymentMethod,
        String note,
        String couponCode
) {
    public record Item(
            @NotNull Long productId,
            @NotNull Long skuId,
            @NotNull @Min(1) Integer quantity
    ) {}
}
