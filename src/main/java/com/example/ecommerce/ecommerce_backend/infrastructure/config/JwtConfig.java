package com.example.ecommerce.ecommerce_backend.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * JWT configuration with automatic secret generation if not provided
 */
@Configuration
@ConfigurationProperties(prefix = "app.security.jwt")
@Data
@Validated
public class JwtConfig {

    private static final Logger log = LoggerFactory.getLogger(JwtConfig.class);

    @NotBlank
    private String issuer;

    @Min(60)
    private long accessTtlSeconds;

    @Min(3600)
    private long refreshTtlSeconds;

    private String secretBase64;

    /**
     * Generate a secure random secret if not provided
     */
    @PostConstruct
    public void init() {
        if (secretBase64 == null || secretBase64.trim().isEmpty()) {
            log.warn("JWT secret not provided, generating a random secret. This is NOT recommended for production!");
            log.warn("Please set APP_SECURITY_JWT_SECRET_BASE64 environment variable with a secure secret.");

            // Generate a 512-bit (64 bytes) secret
            byte[] secret = new byte[64];
            new SecureRandom().nextBytes(secret);
            secretBase64 = Base64.getEncoder().encodeToString(secret);

            log.info("Generated JWT secret (base64): {}", maskSecret(secretBase64));
        } else {
            // Validate secret strength
            byte[] decoded = Base64.getDecoder().decode(secretBase64);
            if (decoded.length < 32) {
                log.warn("JWT secret is too short ({}bytes). Recommended minimum is 32 bytes (256 bits)", decoded.length);
            }
            log.info("Using configured JWT secret");
        }
    }

    /**
     * Mask the secret for logging (show only first and last 4 characters)
     */
    private String maskSecret(String secret) {
        if (secret.length() <= 8) {
            return "********";
        }
        return secret.substring(0, 4) + "..." + secret.substring(secret.length() - 4);
    }

    /**
     * Get the decoded secret key bytes
     */
    public byte[] getSecretKey() {
        return Base64.getDecoder().decode(secretBase64);
    }
}