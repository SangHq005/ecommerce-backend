package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;
import org.springframework.stereotype.Component;

@Component
public class CouponDiscountStrategy implements DiscountStrategy {

    private final CouponService couponService;

    public CouponDiscountStrategy(CouponService couponService) {
        this.couponService = couponService;
    }

    @Override
    public boolean isApplicable(DiscountContext ctx) {
        return ctx.couponCode() != null && !ctx.couponCode().isBlank();
    }

    @Override
    public DiscountResult calculate(DiscountContext ctx) {
        var response = couponService.validateCoupon(
            ctx.couponCode(),
            ctx.userId(),
            ctx.totalAmount(),
            ctx.productIds(),
            ctx.categoryIds()
        );
        if (response.valid()) {
            return new DiscountResult(response.discountAmount(), "Coupon: " + ctx.couponCode());
        }
        return DiscountResult.zero();
    }
}
