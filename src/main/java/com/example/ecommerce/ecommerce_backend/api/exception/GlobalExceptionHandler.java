package com.example.ecommerce.ecommerce_backend.api.exception;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.example.ecommerce.ecommerce_backend.api.filter.CorrelationIdFilter;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

/**
 * Global Exception Handler.
 * 
 * Transforms all exceptions into standardized ApiResponse format.
 * Ensures consistent error response structure across all endpoints.
 */
@RestControllerAdvice(basePackages = "com.example.ecommerce.ecommerce_backend.api")
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ========== VALIDATION ERRORS ==========

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ApiResponse.FieldError> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ApiResponse.FieldError(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.toList());

        log.warn("Validation failed: {} errors on {}", fieldErrors.size(), request.getRequestURI());

        return ResponseEntity.badRequest().body(
                ApiResponse.validationError(fieldErrors, getCorrelationId(), request.getRequestURI())
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<ApiResponse.FieldError> fieldErrors = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return new ApiResponse.FieldError(field, cv.getMessage());
                })
                .collect(Collectors.toList());

        log.warn("Constraint violation: {} on {}", fieldErrors.size(), request.getRequestURI());

        return ResponseEntity.badRequest().body(
                ApiResponse.validationError(fieldErrors, getCorrelationId(), request.getRequestURI())
        );
    }

    // ========== BUSINESS EXCEPTIONS ==========

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        ErrorCode code = ex.getErrorCode();
        log.warn("Business exception: code={}, message={}, path={}",
                code.name(), ex.getMessage(), request.getRequestURI());

        return ResponseEntity.status(code.getHttpStatus()).body(
                errorResponse(code, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.warn("API Exception: code={}, status={}, msg={}, path={}",
                ex.getCode(), ex.getStatus(), ex.getMessage(), request.getRequestURI());

        // Map legacy ApiException to ErrorCode if possible
        ErrorCode code = mapLegacyCode(ex.getCode(), ex.getStatus());
        return ResponseEntity.status(ex.getStatus()).body(
                errorResponse(code, ex.getMessage(), request)
        );
    }

    // ========== DOMAIN-SPECIFIC EXCEPTIONS (Legacy Support) ==========

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Product not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                errorResponse(ErrorCode.PRODUCT_NOT_FOUND, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleOrderNotFound(
            OrderNotFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Order not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                errorResponse(ErrorCode.ORDER_NOT_FOUND, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Void>> handleInsufficientStock(
            InsufficientStockException ex,
            HttpServletRequest request
    ) {
        log.warn("Insufficient stock: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                errorResponse(ErrorCode.INSUFFICIENT_STOCK, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(InvalidCouponException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCoupon(
            InvalidCouponException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid coupon: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                errorResponse(ErrorCode.COUPON_NOT_APPLICABLE, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedAccess(
            UnauthorizedAccessException ex,
            HttpServletRequest request
    ) {
        log.warn("Unauthorized access: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                errorResponse(ErrorCode.AUTH_ACCESS_DENIED, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiResponse<Void>> handlePaymentFailed(
            PaymentFailedException ex,
            HttpServletRequest request
    ) {
        log.error("Payment failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                errorResponse(ErrorCode.PAYMENT_FAILED, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidOperation(
            InvalidOperationException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid operation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                errorResponse(ErrorCode.BUSINESS_RULE_VIOLATION, ex.getMessage(), request)
        );
    }

    // ========== SECURITY EXCEPTIONS ==========

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ErrorCode code = (ex instanceof BadCredentialsException)
                ? ErrorCode.AUTH_INVALID_CREDENTIALS
                : ErrorCode.UNAUTHORIZED;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                errorResponse(code, ex.getMessage(), request)
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        log.warn("Access denied: {} on {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                errorResponse(ErrorCode.AUTH_ACCESS_DENIED, "You don't have permission to access this resource", request)
        );
    }

    // ========== REQUEST ERRORS ==========

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                errorResponse(ErrorCode.BAD_REQUEST, "Invalid request body format", request)
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Method not supported: {} on {}", ex.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                errorResponse(ErrorCode.BAD_REQUEST, "HTTP method " + ex.getMethod() + " not supported", request)
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpServletRequest request
    ) {
        log.warn("Media type not supported: {}", ex.getContentType());
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(
                errorResponse(ErrorCode.BAD_REQUEST, "Content type not supported", request)
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        log.warn("Missing parameter: {}", ex.getParameterName());
        return ResponseEntity.badRequest().body(
                ApiResponse.validationError(
                        ex.getParameterName(),
                        "Required parameter is missing",
                        getCorrelationId(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        log.warn("Type mismatch: {} expected {}", ex.getName(), ex.getRequiredType());
        String message = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return ResponseEntity.badRequest().body(
                ApiResponse.validationError(ex.getName(), message, getCorrelationId(), request.getRequestURI())
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoHandler(
            NoHandlerFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                errorResponse(ErrorCode.NOT_FOUND, "Endpoint not found", request)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(
                errorResponse(ErrorCode.BAD_REQUEST, ex.getMessage(), request)
        );
    }

    // ========== CATCH-ALL HANDLER ==========

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(
            Exception ex,
            HttpServletRequest request
    ) {
        String path = request.getRequestURI();

        // Don't wrap springdoc/swagger endpoints
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            throw new RuntimeException(ex);
        }

        // Log full exception with correlationId for debugging
        String cid = getCorrelationId();
        log.error("Unhandled exception. cid={}, path={}, type={}, message={}",
                cid, path, ex.getClass().getSimpleName(), ex.getMessage(), ex);

        // For debugging: expose the error message in the response
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "An unexpected error occurred (" + ex.getClass().getSimpleName() + ")";
        } else {
            message = message + " (" + ex.getClass().getSimpleName() + ")";
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ApiResponse<>(
                        false,
                        ErrorCode.INTERNAL_ERROR.name(),
                        message,
                        null,
                        null,
                        null,
                        Instant.now(),
                        cid,
                        path
                )
        );
    }

    // ========== HELPER METHODS ==========

    private String getCorrelationId() {
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        return cid != null ? cid : "unknown";
    }

    private ApiResponse<Void> errorResponse(ErrorCode code, String message, HttpServletRequest request) {
        return new ApiResponse<>(
                false,
                code.name(),
                message,
                null,
                null,
                null,
                Instant.now(),
                getCorrelationId(),
                request.getRequestURI()
        );
    }

    private ErrorCode mapLegacyCode(String code, int status) {
        try {
            return ErrorCode.valueOf(code);
        } catch (IllegalArgumentException e) {
            // Map by status code
            return switch (status) {
                case 400 -> ErrorCode.BAD_REQUEST;
                case 401 -> ErrorCode.UNAUTHORIZED;
                case 403 -> ErrorCode.AUTH_ACCESS_DENIED;
                case 404 -> ErrorCode.NOT_FOUND;
                case 409 -> ErrorCode.CONFLICT;
                case 422 -> ErrorCode.BUSINESS_RULE_VIOLATION;
                default -> ErrorCode.INTERNAL_ERROR;
            };
        }
    }
}
