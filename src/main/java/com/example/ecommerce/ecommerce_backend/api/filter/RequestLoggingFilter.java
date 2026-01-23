package com.example.ecommerce.ecommerce_backend.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Filter to log all HTTP requests and responses with correlation IDs
 */
@Component
@Order(1) // Run early in the filter chain
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
        "/health", "/swagger-ui", "/v3/api-docs", "/uploads"
    );
    private static final int MAX_PAYLOAD_LENGTH = 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Skip logging for excluded paths
        if (shouldSkipLogging(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache content
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Generate or retrieve correlation ID
        String correlationId = getOrCreateCorrelationId(request);
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(wrappedRequest, correlationId);

            // Process request
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Log response
            long duration = System.currentTimeMillis() - startTime;
            logResponse(wrappedRequest, wrappedResponse, duration, correlationId);

        } finally {
            // Copy response content to actual response
            wrappedResponse.copyBodyToResponse();

            // Clear MDC
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private boolean shouldSkipLogging(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    private String getOrCreateCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (!StringUtils.hasText(correlationId)) {
            correlationId = "c-" + UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void logRequest(ContentCachingRequestWrapper request, String correlationId) {
        StringBuilder msg = new StringBuilder();
        msg.append("REQUEST [").append(correlationId).append("] ");
        msg.append(request.getMethod()).append(" ");
        msg.append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null) {
            msg.append("?").append(queryString);
        }

        String client = request.getRemoteAddr();
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            client = xForwardedFor.split(",")[0].trim();
        }
        msg.append(" from ").append(client);

        // Log request body for POST/PUT/PATCH in debug mode
        if (log.isDebugEnabled() && isBodyLoggable(request)) {
            String body = getRequestBody(request);
            if (StringUtils.hasText(body)) {
                msg.append(" Body: ").append(truncate(maskSensitiveData(body)));
            }
        }

        log.info(msg.toString());
    }

    private void logResponse(ContentCachingRequestWrapper request,
                              ContentCachingResponseWrapper response,
                              long duration,
                              String correlationId) {
        StringBuilder msg = new StringBuilder();
        msg.append("RESPONSE [").append(correlationId).append("] ");
        msg.append(request.getMethod()).append(" ");
        msg.append(request.getRequestURI());
        msg.append(" ").append(response.getStatus());
        msg.append(" ").append(duration).append("ms");

        // Log response body in debug mode for non-success status
        if (log.isDebugEnabled() && response.getStatus() >= 400) {
            String body = getResponseBody(response);
            if (StringUtils.hasText(body)) {
                msg.append(" Body: ").append(truncate(body));
            }
        }

        // Use different log levels based on status code
        if (response.getStatus() >= 500) {
            log.error(msg.toString());
        } else if (response.getStatus() >= 400) {
            log.warn(msg.toString());
        } else {
            log.info(msg.toString());
        }
    }

    private boolean isBodyLoggable(HttpServletRequest request) {
        String method = request.getMethod();
        String contentType = request.getContentType();

        // Only log JSON bodies for certain methods
        return ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method))
                && contentType != null
                && contentType.contains("application/json");
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, request.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unable to read body]";
            }
        }
        return "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, response.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                return "[Unable to read body]";
            }
        }
        return "";
    }

    private String maskSensitiveData(String body) {
        // Mask passwords and tokens in logs
        return body.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                   .replaceAll("\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"***\"")
                   .replaceAll("\"refreshToken\"\\s*:\\s*\"[^\"]*\"", "\"refreshToken\":\"***\"")
                   .replaceAll("\"accessToken\"\\s*:\\s*\"[^\"]*\"", "\"accessToken\":\"***\"")
                   .replaceAll("\"secret\"\\s*:\\s*\"[^\"]*\"", "\"secret\":\"***\"");
    }

    private String truncate(String text) {
        if (text.length() <= MAX_PAYLOAD_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_PAYLOAD_LENGTH) + "... [truncated]";
    }
}