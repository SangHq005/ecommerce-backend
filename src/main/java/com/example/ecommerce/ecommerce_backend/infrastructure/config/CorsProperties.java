package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalized CORS configuration properties.
 * Configure allowed origins via environment variables or application properties.
 */
@Component
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

    /**
     * List of allowed origins for CORS requests.
     * Can be configured via APP_CORS_ALLOWED_ORIGINS environment variable.
     */
    private List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:4200",
            "http://localhost:5173",
            "http://localhost:3001"
    );

    /**
     * List of allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    );

    /**
     * List of allowed headers.
     */
    private List<String> allowedHeaders = List.of("*");

    /**
     * Whether to allow credentials (cookies, authorization headers).
     */
    private boolean allowCredentials = true;

    /**
     * Max age in seconds for preflight cache.
     */
    private long maxAge = 3600L;

    /**
     * Headers exposed to the client.
     */
    private List<String> exposedHeaders = List.of(
            "Authorization",
            "X-Correlation-ID",
            "X-Total-Count"
    );

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }

    public List<String> getExposedHeaders() {
        return exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }
}
