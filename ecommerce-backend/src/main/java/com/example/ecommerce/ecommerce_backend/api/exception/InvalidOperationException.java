package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when an invalid operation is attempted
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
