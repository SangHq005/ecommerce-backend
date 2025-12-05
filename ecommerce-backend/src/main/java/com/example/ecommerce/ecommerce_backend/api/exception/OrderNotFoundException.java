package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when a requested order is not found
 */
public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(Long orderId) {
        super("Order not found with id: " + orderId);
    }

    public OrderNotFoundException(String orderCode) {
        super("Order not found with code: " + orderCode);
    }
}
