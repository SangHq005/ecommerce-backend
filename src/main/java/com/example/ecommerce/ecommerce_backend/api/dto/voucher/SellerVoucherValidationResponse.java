package com.example.ecommerce.ecommerce_backend.api.dto.voucher;

/**
 * Response DTO for voucher validation result.
 */
public record SellerVoucherValidationResponse(
    boolean valid,
    String code,
    String name,
    Long discountAmount,
    Long originalTotal,
    Long finalTotal,
    String message,
    String errorCode // null if valid, otherwise: EXPIRED, NOT_FOUND, LIMIT_REACHED, MIN_ORDER_NOT_MET, etc.
) {
    public static SellerVoucherValidationResponse valid(
            String code, 
            String name,
            Long discountAmount, 
            Long originalTotal
    ) {
        return new SellerVoucherValidationResponse(
            true, code, name, discountAmount, originalTotal, 
            originalTotal - discountAmount, 
            "Voucher applied successfully", null
        );
    }
    
    public static SellerVoucherValidationResponse invalid(String code, String message, String errorCode) {
        return new SellerVoucherValidationResponse(
            false, code, null, 0L, null, null, message, errorCode
        );
    }
}
