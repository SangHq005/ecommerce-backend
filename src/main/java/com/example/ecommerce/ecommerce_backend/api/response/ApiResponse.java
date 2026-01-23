package com.example.ecommerce.ecommerce_backend.api.response;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Unified API Response Envelope.
 * 
 * All API endpoints MUST return this structure for consistency.
 * 
 * @param <T> The type of data payload
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"success", "code", "message", "data", "errors", "meta", "timestamp", "correlationId", "path"})
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data,
        List<FieldError> errors,
        PaginationMeta meta,
        Instant timestamp,
        String correlationId,
        String path
) {

    /**
     * Field-level validation error.
     */
    public record FieldError(String field, String message) {}

    /**
     * Pagination metadata for list responses.
     */
    public record PaginationMeta(int page, int size, long total, int totalPages) {
        public static PaginationMeta of(int page, int size, long total) {
            int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
            return new PaginationMeta(page, size, total, totalPages);
        }
    }

    // ========== SUCCESS BUILDERS ==========

    /**
     * Success response with data.
     */
    public static <T> ApiResponse<T> success(T data, String message, String correlationId, String path) {
        return new ApiResponse<>(
                true,
                ErrorCode.SUCCESS.name(),
                message,
                data,
                null,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }

    /**
     * Success response with data (default message).
     */
    public static <T> ApiResponse<T> success(T data, String correlationId, String path) {
        return success(data, "Request processed successfully", correlationId, path);
    }

    /**
     * Success response for resource creation (HTTP 201).
     */
    public static <T> ApiResponse<T> created(T data, String message, String correlationId, String path) {
        return new ApiResponse<>(
                true,
                ErrorCode.CREATED.name(),
                message,
                data,
                null,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }

    /**
     * Success response with pagination.
     */
    public static <T> ApiResponse<T> successWithPagination(T data, PaginationMeta meta, String correlationId, String path) {
        return new ApiResponse<>(
                true,
                ErrorCode.SUCCESS.name(),
                "Request processed successfully",
                data,
                null,
                meta,
                Instant.now(),
                correlationId,
                path
        );
    }

    /**
     * No content response (HTTP 204 equivalent).
     */
    public static ApiResponse<Void> noContent(String correlationId, String path) {
        return new ApiResponse<>(
                true,
                ErrorCode.NO_CONTENT.name(),
                "Operation completed successfully",
                null,
                null,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }

    // ========== ERROR BUILDERS ==========

    /**
     * Error response with code and message.
     */
    public static <T> ApiResponse<T> error(ErrorCode code, String message, String correlationId, String path) {
        return new ApiResponse<>(
                false,
                code.name(),
                message,
                null,
                null,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }

    /**
     * Error response with field errors (validation).
     */
    public static <T> ApiResponse<T> validationError(List<FieldError> errors, String correlationId, String path) {
        return new ApiResponse<>(
                false,
                ErrorCode.VALIDATION_ERROR.name(),
                "Validation failed. Please check your input.",
                null,
                errors,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }

    /**
     * Error response with single field error.
     */
    public static <T> ApiResponse<T> validationError(String field, String message, String correlationId, String path) {
        return validationError(List.of(new FieldError(field, message)), correlationId, path);
    }

    /**
     * Internal server error (hide details from client).
     */
    public static <T> ApiResponse<T> internalError(String correlationId, String path) {
        return new ApiResponse<>(
                false,
                ErrorCode.INTERNAL_ERROR.name(),
                "An unexpected error occurred. Please try again later.",
                null,
                null,
                null,
                Instant.now(),
                correlationId,
                path
        );
    }
}
