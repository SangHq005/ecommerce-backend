package com.example.ecommerce.ecommerce_backend.application.service.payment;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory that resolves a {@link PaymentGatewayStrategy} by payment method name.
 * <p>
 * Spring auto-discovers every bean that implements {@link PaymentGatewayStrategy}
 * and injects them as a {@code List}.  The factory builds a lookup map keyed by
 * {@link PaymentGatewayStrategy#getPaymentMethod()} (stored in upper-case).
 * </p>
 * <p>
 * To register a new gateway, simply create a {@code @Service} class that
 * implements {@link PaymentGatewayStrategy} — no factory changes needed.
 * </p>
 */
@Component
public class PaymentGatewayFactory {

    private static final Logger log = LoggerFactory.getLogger(PaymentGatewayFactory.class);

    /** key = upper-cased payment method, value = corresponding strategy bean */
    private final Map<String, PaymentGatewayStrategy> registry;

    public PaymentGatewayFactory(List<PaymentGatewayStrategy> strategies) {
        this.registry = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getPaymentMethod().toUpperCase(),
                        Function.identity()
                ));
        log.info("PaymentGatewayFactory initialized with {} strategies: {}",
                registry.size(), registry.keySet());
    }

    /**
     * Returns the strategy for the given payment method.
     *
     * @param paymentMethod e.g. "VNPAY", "MOMO", "COD" (case-insensitive)
     * @return the matching {@link PaymentGatewayStrategy}
     * @throws ApiException with HTTP 400 if no strategy is registered for this method
     */
    public PaymentGatewayStrategy getStrategy(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw ApiException.badRequest("Payment method must not be blank");
        }

        String key = paymentMethod.toUpperCase();
        PaymentGatewayStrategy strategy = registry.get(key);

        if (strategy == null) {
            throw ApiException.badRequest(
                    "Unsupported payment method: '" + paymentMethod + "'. " +
                    "Supported methods: " + registry.keySet()
            );
        }

        return strategy;
    }

    /**
     * Checks whether a strategy exists for the given payment method without throwing.
     *
     * @param paymentMethod payment method name (case-insensitive)
     * @return {@code true} if a strategy is registered
     */
    public boolean supports(String paymentMethod) {
        if (paymentMethod == null) return false;
        return registry.containsKey(paymentMethod.toUpperCase());
    }
}
