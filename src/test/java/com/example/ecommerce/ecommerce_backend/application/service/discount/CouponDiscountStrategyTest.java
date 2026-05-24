package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponDiscountStrategyTest {

    @Mock CouponService couponService;
    @InjectMocks CouponDiscountStrategy strategy;

    @Test
    void isApplicable_returnsFalse_whenNoCouponCode() {
        var ctx = new DiscountContext(1L, 2L, 100000L, null, null, List.of(), List.of());
        assertFalse(strategy.isApplicable(ctx));
    }

    @Test
    void isApplicable_returnsTrue_whenCouponCodePresent() {
        var ctx = new DiscountContext(1L, 2L, 100000L, "SUMMER10", null, List.of(), List.of());
        assertTrue(strategy.isApplicable(ctx));
    }

    @Test
    void calculate_returnsDiscountAmount_forValidCoupon() {
        var ctx = new DiscountContext(1L, 2L, 200000L, "SUMMER10", null, List.of(10L), List.of(5L));
        var validationResponse = CouponValidationResponse.valid(20000L, "SUMMER10", "Summer Discount");
        
        when(couponService.validateCoupon("SUMMER10", 1L, 200000L, List.of(10L), List.of(5L)))
            .thenReturn(validationResponse);

        var result = strategy.calculate(ctx);

        assertEquals(20000L, result.discountAmount());
        assertEquals("Coupon: SUMMER10", result.description());
    }

    @Test
    void calculate_returnsZero_whenCouponInvalid() {
        var ctx = new DiscountContext(1L, 2L, 200000L, "INVALID", null, List.of(), List.of());
        var validationResponse = CouponValidationResponse.invalid("Expired");
        
        when(couponService.validateCoupon("INVALID", 1L, 200000L, List.of(), List.of()))
            .thenReturn(validationResponse);

        var result = strategy.calculate(ctx);

        assertEquals(0L, result.discountAmount());
    }
}
