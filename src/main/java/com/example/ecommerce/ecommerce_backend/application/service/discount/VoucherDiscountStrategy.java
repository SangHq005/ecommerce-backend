package com.example.ecommerce.ecommerce_backend.application.service.discount;

import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerVoucherService;
import com.example.ecommerce.ecommerce_backend.api.dto.voucher.ValidateSellerVoucherRequest;
import org.springframework.stereotype.Component;

@Component
public class VoucherDiscountStrategy implements DiscountStrategy {

    private final SellerVoucherService voucherService;

    public VoucherDiscountStrategy(SellerVoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @Override
    public boolean isApplicable(DiscountContext ctx) {
        return ctx.voucherCode() != null && !ctx.voucherCode().isBlank() && ctx.shopId() != null;
    }

    @Override
    public DiscountResult calculate(DiscountContext ctx) {
        ValidateSellerVoucherRequest request = new ValidateSellerVoucherRequest(
            ctx.voucherCode(),
            ctx.shopId(),
            ctx.totalAmount(),
            ctx.productIds(),
            ctx.categoryIds()
        );
        var response = voucherService.validateVoucher(ctx.userId(), request);
        if (response.valid()) {
            return new DiscountResult(response.discountAmount(), "Voucher: " + ctx.voucherCode());
        }
        return DiscountResult.zero();
    }
}
