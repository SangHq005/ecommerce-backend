package com.example.ecommerce.ecommerce_backend.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter to add security headers to all responses
 */
@Component
@Order(2) // Run after request logging
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Prevent content type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Enable XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Control referrer information
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Content Security Policy (adjust based on your needs)
        String csp = "default-src 'self'; " +
                     "script-src 'self' 'unsafe-inline' 'unsafe-eval' https://apis.google.com; " +
                     "style-src 'self' 'unsafe-inline'; " +
                     "img-src 'self' data: https:; " +
                     "font-src 'self' data:; " +
                     "connect-src 'self' https://api.vnpay.vn; " +
                     "frame-src 'none'; " +
                     "object-src 'none'; " +
                     "base-uri 'self'; " +
                     "form-action 'self'; " +
                     "upgrade-insecure-requests";
        response.setHeader("Content-Security-Policy", csp);

        // Permissions Policy (formerly Feature Policy)
        response.setHeader("Permissions-Policy",
            "geolocation=(), microphone=(), camera=(), payment=(), usb=(), magnetometer=()");

        // HSTS - Only enable in production with HTTPS
        if (isProductionEnvironment() && isSecureConnection(request)) {
            response.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");
        }

        filterChain.doFilter(request, response);
    }

    private boolean isSecureConnection(HttpServletRequest request) {
        return request.isSecure() ||
               "https".equals(request.getHeader("X-Forwarded-Proto")) ||
               "on".equals(request.getHeader("X-Forwarded-Ssl"));
    }

    private boolean isProductionEnvironment() {
        String profile = System.getProperty("spring.profiles.active", "");
        return profile.contains("prod");
    }
}