package com.example.ecommerce.ecommerce_backend.application.service.discount;

public interface DiscountStrategy {
    boolean isApplicable(DiscountContext ctx);
    DiscountResult calculate(DiscountContext ctx);
}
