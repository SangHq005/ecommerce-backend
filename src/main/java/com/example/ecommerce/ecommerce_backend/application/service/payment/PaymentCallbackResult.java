package com.example.ecommerce.ecommerce_backend.application.service.payment;

import java.util.Map;

/**
 * Value Object representing the normalized result of a payment gateway callback.
 * <p>
 * Each PaymentGatewayStrategy maps its raw gateway-specific response to this
 * common structure, allowing PaymentService to process callbacks in a
 * gateway-agnostic way.
 * </p>
 *
 * @param success       {@code true} if the payment was completed successfully
 * @param transactionId Gateway-side transaction identifier (may be null on failure)
 * @param orderCode     Platform-side order code extracted from the callback
 * @param rawResponse   Raw key-value pairs from the gateway, stored for auditing
 */
public record PaymentCallbackResult(
        boolean success,
        String transactionId,
        String orderCode,
        Map<String, Object> rawResponse
) {

    /** Factory: success outcome. */
    public static PaymentCallbackResult success(
            String transactionId,
            String orderCode,
            Map<String, Object> rawResponse) {
        return new PaymentCallbackResult(true, transactionId, orderCode, rawResponse);
    }

    /** Factory: failure outcome. */
    public static PaymentCallbackResult failure(
            String orderCode,
            Map<String, Object> rawResponse) {
        return new PaymentCallbackResult(false, null, orderCode, rawResponse);
    }
}
