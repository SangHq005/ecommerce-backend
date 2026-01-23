package com.example.ecommerce.ecommerce_backend.api.response;

import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.ecommerce.ecommerce_backend.api.filter.CorrelationIdFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Utility class for building standardized API responses.
 * 
 * Controllers should use this helper to ensure consistent response format.
 */
public final class ResponseHelper {

    private ResponseHelper() {
        // Utility class
    }

    // ========== CONTEXT HELPERS ==========

    /**
     * Get current correlation ID from MDC.
     */
    public static String getCorrelationId() {
        String cid = MDC.get(CorrelationIdFilter.MDC_KEY);
        return cid != null ? cid : "unknown";
    }

    /**
     * Get current request path.
     */
    public static String getRequestPath() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getRequestURI();
            }
        } catch (Exception ignored) {
        }
        return "/unknown";
    }

    // ========== SUCCESS RESPONSES ==========

    /**
     * OK response (HTTP 200) with data.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data, getCorrelationId(), getRequestPath()));
    }

    /**
     * OK response (HTTP 200) with data and custom message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> ok(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message, getCorrelationId(), getRequestPath()));
    }

    /**
     * Created response (HTTP 201) with data.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, "Resource created successfully", getCorrelationId(), getRequestPath()));
    }

    /**
     * Created response (HTTP 201) with data and custom message.
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, message, getCorrelationId(), getRequestPath()));
    }

    /**
     * No Content response (HTTP 204).
     */
    public static ResponseEntity<ApiResponse<Void>> noContent() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.noContent(getCorrelationId(), getRequestPath()));
    }

    /**
     * OK response with empty body (alternative to 204).
     */
    public static ResponseEntity<ApiResponse<Void>> okNoContent() {
        return ResponseEntity.ok(ApiResponse.noContent(getCorrelationId(), getRequestPath()));
    }

    // ========== PAGINATED RESPONSES ==========

    /**
     * Paginated response from Spring Data Page.
     */
    public static <T> ResponseEntity<ApiResponse<java.util.List<T>>> page(Page<T> page) {
        ApiResponse.PaginationMeta meta = ApiResponse.PaginationMeta.of(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.successWithPagination(
                page.getContent(),
                meta,
                getCorrelationId(),
                getRequestPath()
        ));
    }

    /**
     * Paginated response with custom data transformation.
     */
    public static <T, R> ResponseEntity<ApiResponse<java.util.List<R>>> page(
            Page<T> page,
            java.util.function.Function<T, R> mapper
    ) {
        java.util.List<R> content = page.getContent().stream().map(mapper).toList();
        ApiResponse.PaginationMeta meta = ApiResponse.PaginationMeta.of(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
        return ResponseEntity.ok(ApiResponse.successWithPagination(
                content,
                meta,
                getCorrelationId(),
                getRequestPath()
        ));
    }

    // ========== ERROR RESPONSES ==========

    /**
     * Error response with ErrorCode.
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode code, String message) {
        return ResponseEntity.status(code.getHttpStatus())
                .body(ApiResponse.error(code, message, getCorrelationId(), getRequestPath()));
    }

    /**
     * Error response with ErrorCode (default message).
     */
    public static <T> ResponseEntity<ApiResponse<T>> error(ErrorCode code) {
        return error(code, code.getDefaultMessage());
    }

    /**
     * Validation error response.
     */
    public static <T> ResponseEntity<ApiResponse<T>> validationError(
            java.util.List<ApiResponse.FieldError> errors
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError(errors, getCorrelationId(), getRequestPath()));
    }

    /**
     * Single field validation error.
     */
    public static <T> ResponseEntity<ApiResponse<T>> validationError(String field, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.validationError(field, message, getCorrelationId(), getRequestPath()));
    }

    /**
     * Internal server error (hides details).
     */
    public static <T> ResponseEntity<ApiResponse<T>> internalError() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.internalError(getCorrelationId(), getRequestPath()));
    }
}
