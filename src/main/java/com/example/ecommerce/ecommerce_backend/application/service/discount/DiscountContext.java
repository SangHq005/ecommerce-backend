package com.example.ecommerce.ecommerce_backend.application.service.discount;

import java.util.List;

public record DiscountContext(
    Long userId,
    Long shopId,
    long totalAmount,
    String couponCode,
    String voucherCode,
    List<Long> productIds,
    List<Long> categoryIds
) {}
