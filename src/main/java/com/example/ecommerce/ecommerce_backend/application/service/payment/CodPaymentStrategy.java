package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Concrete Strategy: Cash-On-Delivery (COD).
 * <p>
 * COD orders are considered "paid" at checkout time — no payment gateway is involved.
 * This strategy is a no-op for URL creation; it exists so that COD participates
 * in the same {@link PaymentGatewayStrategy} contract as online gateways, keeping
 * {@link PaymentGatewayFactory} the single resolution point for all payment methods.
 * </p>
 */
@Service
public class CodPaymentStrategy implements PaymentGatewayStrategy {

    private static final Logger log = LoggerFactory.getLogger(CodPaymentStrategy.class);

    @Override
    public String getPaymentMethod() {
        return "COD";
    }

    /**
     * COD does not redirect the user to any payment page.
     * Returns {@code null} — callers must check for this and skip the redirect.
     */
    @Override
    public String createPaymentUrl(OrderEntity order, String clientIp) {
        log.info("COD order {} — no payment URL needed", order.getOrderCode());
        return null;
    }

    /**
     * COD has no gateway callback; always returns {@code true} so the
     * strategy contract is satisfied without throwing.
     */
    @Override
    public boolean validateCallback(Map<String, String> params) {
        return true;
    }

    /**
     * COD is auto-confirmed at checkout, so a callback represents an
     * immediate success with no transaction ID.
     */
    @Override
    public PaymentCallbackResult processCallback(Map<String, String> params) {
        String orderCode = params.getOrDefault("orderCode", "");
        log.info("COD auto-confirm for order: {}", orderCode);
        return PaymentCallbackResult.success(null, orderCode, Map.copyOf(params));
    }
}
