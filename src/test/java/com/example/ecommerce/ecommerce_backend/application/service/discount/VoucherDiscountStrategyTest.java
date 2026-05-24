package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.api.dto.voucher.SellerVoucherValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.ValidateSellerVoucherRequest;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerVoucherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherDiscountStrategyTest {

    @Mock SellerVoucherService voucherService;
    @InjectMocks VoucherDiscountStrategy strategy;

    @Test
    void isApplicable_returnsFalse_whenNoVoucherCodeOrShopId() {
        var ctx1 = new DiscountContext(1L, null, 100000L, null, "SHOP10", List.of(), List.of());
        var ctx2 = new DiscountContext(1L, 2L, 100000L, null, null, List.of(), List.of());
        
        assertFalse(strategy.isApplicable(ctx1));
        assertFalse(strategy.isApplicable(ctx2));
    }

    @Test
    void isApplicable_returnsTrue_whenVoucherCodeAndShopIdPresent() {
        var ctx = new DiscountContext(1L, 2L, 100000L, null, "SHOP10", List.of(), List.of());
        assertTrue(strategy.isApplicable(ctx));
    }

    @Test
    void calculate_returnsDiscountAmount_forValidVoucher() {
        var ctx = new DiscountContext(1L, 2L, 200000L, null, "SHOP10", List.of(10L), List.of(5L));
        var request = new ValidateSellerVoucherRequest("SHOP10", 2L, 200000L, List.of(10L), List.of(5L));
        var validationResponse = SellerVoucherValidationResponse.valid("SHOP10", "Shop Discount", 15000L, 200000L);
        
        when(voucherService.validateVoucher(1L, request)).thenReturn(validationResponse);

        var result = strategy.calculate(ctx);

        assertEquals(15000L, result.discountAmount());
        assertEquals("Voucher: SHOP10", result.description());
    }

    @Test
    void calculate_returnsZero_whenVoucherInvalid() {
        var ctx = new DiscountContext(1L, 2L, 200000L, null, "INVALID", List.of(), List.of());
        var request = new ValidateSellerVoucherRequest("INVALID", 2L, 200000L, List.of(), List.of());
        var validationResponse = SellerVoucherValidationResponse.invalid("INVALID", "Expired", "EXPIRED");
        
        when(voucherService.validateVoucher(1L, request)).thenReturn(validationResponse);

        var result = strategy.calculate(ctx);

        assertEquals(0L, result.discountAmount());
    }
}
