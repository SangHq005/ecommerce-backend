package com.example.ecommerce.ecommerce_backend.api.response;

import org.springframework.http.HttpStatus;

/**
 * Centralized Error Code Catalog.
 * 
 * Business codes are grouped by domain and mapped to HTTP status codes.
 * Frontend should switch on these codes for business logic handling.
 */
public enum ErrorCode {

    // ========== SUCCESS CODES ==========
    SUCCESS(HttpStatus.OK, "Operation successful"),
    CREATED(HttpStatus.CREATED, "Resource created successfully"),
    NO_CONTENT(HttpStatus.NO_CONTENT, "Operation completed with no content"),

    // ========== AUTH ERRORS (401, 403) ==========
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication required"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Access token has expired"),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid access token"),
    AUTH_REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Refresh token has expired"),
    AUTH_REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "Invalid refresh token"),
    AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN, "Access denied"),
    AUTH_ACCOUNT_DISABLED(HttpStatus.FORBIDDEN, "Account has been disabled"),
    AUTH_EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN, "Email not verified"),

    // ========== VALIDATION ERRORS (400) ==========
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Validation failed"),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Invalid request"),
    INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "Invalid parameter value"),

    // ========== NOT FOUND ERRORS (404) ==========
    NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "User not found"),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "Product not found"),
    SKU_NOT_FOUND(HttpStatus.NOT_FOUND, "SKU not found"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "Order not found"),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "Cart item not found"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "Category not found"),
    BRAND_NOT_FOUND(HttpStatus.NOT_FOUND, "Brand not found"),
    SHOP_NOT_FOUND(HttpStatus.NOT_FOUND, "Shop not found"),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "Coupon not found"),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "Payment not found"),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "Review not found"),
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "Address not found"),
    REFUND_NOT_FOUND(HttpStatus.NOT_FOUND, "Refund not found"),

    // ========== CONFLICT ERRORS (409) ==========
    CONFLICT(HttpStatus.CONFLICT, "Resource conflict"),
    CHECKOUT_IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT, "Duplicate checkout request detected"),
    OUT_OF_STOCK(HttpStatus.CONFLICT, "Product is out of stock"),
    STOCK_RESERVATION_CONFLICT(HttpStatus.CONFLICT, "Stock reservation conflict"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "Email is already registered"),
    SHOP_ALREADY_EXISTS(HttpStatus.CONFLICT, "Shop already exists for this user"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "Review already submitted for this order"),

    // ========== BUSINESS RULE VIOLATIONS (422) ==========
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY, "Business rule violated"),
    ORDER_NOT_CANCELLABLE(HttpStatus.UNPROCESSABLE_ENTITY, "Order cannot be cancelled in current state"),
    ORDER_NOT_REFUNDABLE(HttpStatus.UNPROCESSABLE_ENTITY, "Order cannot be refunded"),
    ORDER_ALREADY_PAID(HttpStatus.UNPROCESSABLE_ENTITY, "Order has already been paid"),
    ORDER_INVALID_STATE(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid order state for this operation"),
    COUPON_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon has expired"),
    COUPON_NOT_APPLICABLE(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon is not applicable to this order"),
    COUPON_USAGE_LIMIT_REACHED(HttpStatus.UNPROCESSABLE_ENTITY, "Coupon usage limit reached"),
    COUPON_MIN_ORDER_NOT_MET(HttpStatus.UNPROCESSABLE_ENTITY, "Minimum order amount not met for coupon"),
    CART_EMPTY(HttpStatus.UNPROCESSABLE_ENTITY, "Cart is empty"),
    INSUFFICIENT_STOCK(HttpStatus.UNPROCESSABLE_ENTITY, "Insufficient stock available"),
    PRODUCT_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "Product is not active"),
    SKU_INACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "SKU is not active"),

    // ========== PAYMENT ERRORS (402, 422) ==========
    PAYMENT_REQUIRED(HttpStatus.PAYMENT_REQUIRED, "Payment required"),
    PAYMENT_FAILED(HttpStatus.UNPROCESSABLE_ENTITY, "Payment processing failed"),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.UNPROCESSABLE_ENTITY, "Payment has already been processed"),
    PAYMENT_SIGNATURE_INVALID(HttpStatus.UNPROCESSABLE_ENTITY, "Payment signature verification failed"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "Payment amount does not match order"),

    // ========== SELLER ERRORS (403, 422) ==========
    SELLER_NOT_OWNER(HttpStatus.FORBIDDEN, "You are not the owner of this resource"),
    SELLER_SHOP_NOT_ACTIVE(HttpStatus.UNPROCESSABLE_ENTITY, "Seller shop is not active"),
    SELLER_PRODUCT_INVALID_STATE(HttpStatus.UNPROCESSABLE_ENTITY, "Product state is invalid for this operation"),

    // ========== ADMIN ERRORS (403) ==========
    ADMIN_PERMISSION_REQUIRED(HttpStatus.FORBIDDEN, "Admin permission required"),

    // ========== COMPARE ERRORS (400, 404, 422) ==========
    COMPARE_INVALID_INPUT(HttpStatus.BAD_REQUEST, "Invalid comparison input"),
    COMPARE_MIN_PRODUCTS(HttpStatus.BAD_REQUEST, "Minimum 2 products required for comparison"),
    COMPARE_MAX_PRODUCTS(HttpStatus.BAD_REQUEST, "Maximum 4 products allowed for comparison"),
    COMPARE_DUPLICATE_PRODUCTS(HttpStatus.BAD_REQUEST, "Duplicate products in comparison"),
    COMPARE_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "One or more products not found"),
    COMPARE_DIFFERENT_CATEGORY(HttpStatus.UNPROCESSABLE_ENTITY, "Products must be in the same category"),
    COMPARE_PRODUCT_UNAVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY, "One or more products are unavailable"),

    // ========== RATE LIMITING (429) ==========
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later."),

    // ========== SERVER ERRORS (500) ==========
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getStatusCode() {
        return httpStatus.value();
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
