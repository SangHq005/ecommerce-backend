package com.example.ecommerce.ecommerce_backend.api.exception;

import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;

/**
 * Base exception for all business logic errors.
 * 
 * All domain-specific exceptions should extend this class.
 * The ErrorCode determines the HTTP status and business code in the response.
 */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Object[] args;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = null;
    }

    public BusinessException(ErrorCode errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Object[] getArgs() {
        return args;
    }

    public int getHttpStatus() {
        return errorCode.getStatusCode();
    }

    // ========== FACTORY METHODS FOR COMMON ERRORS ==========

    public static BusinessException notFound(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException conflict(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException forbidden(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException unauthorized(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException validation(String message) {
        return new BusinessException(ErrorCode.VALIDATION_ERROR, message);
    }

    public static BusinessException businessRule(ErrorCode code, String message) {
        return new BusinessException(code, message);
    }
}
