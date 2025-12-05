package com.example.ecommerce.ecommerce_backend.api.exception;

import com.example.ecommerce.ecommerce_backend.api.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice(basePackages = "com.example.ecommerce.ecommerce_backend.api")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("Validation failed");
        return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiError> handleProductNotFound(ProductNotFoundException ex) {
        log.warn("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("PRODUCT_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex) {
        log.warn("Order not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error("ORDER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiError> handleInsufficientStock(InsufficientStockException ex) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("INSUFFICIENT_STOCK", ex.getMessage()));
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiError> handleInvalidCoupon(InvalidCouponException ex) {
        log.warn("Invalid coupon: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("INVALID_COUPON", ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiError> handleUnauthorizedAccess(UnauthorizedAccessException ex) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(error("UNAUTHORIZED_ACCESS", ex.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handlePaymentFailed(PaymentFailedException ex) {
        log.error("Payment failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(error("PAYMENT_FAILED", ex.getMessage()));
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiError> handleInvalidOperation(InvalidOperationException ex) {
        log.warn("Invalid operation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(error("INVALID_OPERATION", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnknown(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        // IMPORTANT: do not wrap springdoc/swagger endpoints, let Spring handle default error
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            throw new RuntimeException(ex); // force default handling with stacktrace in logs
        }

        // Log full exception with correlationId for debugging
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        log.error("Unhandled exception. cid={}, path={}", cid, path, ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error("INTERNAL_ERROR", "Unexpected error"));
    }

    private ApiError error(String code, String message) {
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        return new ApiError(code, message, Instant.now(), cid);
    }
}
