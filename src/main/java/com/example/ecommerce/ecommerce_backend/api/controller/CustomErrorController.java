package com.example.ecommerce.ecommerce_backend.api.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;

@RestController
public class CustomErrorController implements org.springframework.boot.web.servlet.error.ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<?> handleError(HttpServletRequest request) {
        String requestUri = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String queryString = request.getQueryString();
        
        // Exclude OAuth2 callback paths - let Spring Security handle them
        if (requestUri != null && (
            requestUri.startsWith("/login/oauth2/") ||
            requestUri.startsWith("/oauth2/") ||
            (queryString != null && (queryString.contains("code=") || queryString.contains("state=")))
        )) {
            // For OAuth2 errors, return a simple error that frontend can handle
            // Don't interfere with OAuth2 flow
            Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
            if (status != null && Integer.parseInt(status.toString()) == 401) {
                // OAuth2 authentication failed - return error that frontend can redirect
                return ResponseHelper.error(ErrorCode.UNAUTHORIZED, "OAuth2 authentication failed. Please try again.");
            }
        }
        
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        
        ErrorCode errorCode = ErrorCode.INTERNAL_ERROR;
        String errorMessage = "An unexpected error occurred";
        
        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());
            
            switch (statusCode) {
                case 401:
                    errorCode = ErrorCode.UNAUTHORIZED;
                    errorMessage = "Unauthorized. Please login to access this resource.";
                    break;
                case 403:
                    errorCode = ErrorCode.AUTH_ACCESS_DENIED;
                    errorMessage = "Forbidden. You don't have permission to access this resource.";
                    break;
                case 404:
                    errorCode = ErrorCode.NOT_FOUND;
                    errorMessage = "Resource not found.";
                    break;
                case 500:
                    errorCode = ErrorCode.INTERNAL_ERROR;
                    errorMessage = "Internal server error.";
                    break;
                default:
                    errorCode = ErrorCode.INTERNAL_ERROR;
                    if (message != null) {
                        errorMessage = message.toString();
                    }
            }
        }
        
        return ResponseHelper.error(errorCode, errorMessage);
    }
}
