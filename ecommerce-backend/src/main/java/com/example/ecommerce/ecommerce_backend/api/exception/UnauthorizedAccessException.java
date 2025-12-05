package com.example.ecommerce.ecommerce_backend.api.exception;

/**
 * Exception thrown when a user attempts unauthorized access to a resource
 */
public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(String resourceType, Long resourceId) {
        super(String.format("Unauthorized access to %s with id: %d", resourceType, resourceId));
    }
}
