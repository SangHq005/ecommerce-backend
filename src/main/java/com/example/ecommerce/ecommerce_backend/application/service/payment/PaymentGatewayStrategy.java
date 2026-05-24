package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;

import java.util.Map;

/**
 * Strategy interface for payment gateway integrations.
 * <p>
 * Each concrete strategy encapsulates all gateway-specific logic (URL
 * construction, signature computation, callback parsing) behind a uniform
 * contract.  Adding a new gateway (e.g. ZaloPay, PayOS) requires only:
 * <ol>
 *   <li>Implementing this interface</li>
 *   <li>Annotating the class with {@code @Service} so Spring picks it up</li>
 * </ol>
 * No changes are needed in {@link com.example.ecommerce.ecommerce_backend.application.service.payment.PaymentService}
 * or {@link PaymentGatewayFactory}.
 * </p>
 */
public interface PaymentGatewayStrategy {

    /**
     * Returns the payment method name this strategy handles.
     * Must match values used in {@code OrderEntity.paymentMethod} (e.g. "VNPAY", "MOMO", "COD").
     */
    String getPaymentMethod();

    /**
     * Generates the redirect URL that will send the user to the gateway's payment page.
     *
     * @param order     the order to pay for
     * @param clientIp  caller IP address (used by some gateways for fraud checks)
     * @return payment URL; never {@code null}
     * @throws com.example.ecommerce.ecommerce_backend.api.exception.ApiException on gateway errors
     */
    String createPaymentUrl(OrderEntity order, String clientIp);

    /**
     * Validates the HMAC / signature of a gateway callback.
     *
     * @param params raw parameters received from the gateway
     * @return {@code true} if the signature is authentic
     */
    boolean validateCallback(Map<String, String> params);

    /**
     * Parses a validated callback and maps it to a normalized {@link PaymentCallbackResult}.
     *
     * @param params raw parameters received from the gateway (already validated)
     * @return a gateway-agnostic result object
     */
    PaymentCallbackResult processCallback(Map<String, String> params);
}
