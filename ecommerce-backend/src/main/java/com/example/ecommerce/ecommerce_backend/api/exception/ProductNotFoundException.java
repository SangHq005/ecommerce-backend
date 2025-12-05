package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when a requested product is not found
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found with id: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
