package com.example.ecommerce.ecommerce_backend.api.aspect;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.ecommerce.ecommerce_backend.api.annotation.RateLimit;
import com.example.ecommerce.ecommerce_backend.application.service.common.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aspect for handling @RateLimit annotation
 * Automatically applies rate limiting to annotated controller methods
 * Note: Rate limiting is disabled in the "test" profile
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);

    private final RateLimitService rateLimitService;
    private final boolean rateLimitEnabled;

    public RateLimitAspect(RateLimitService rateLimitService, Environment environment) {
        this.rateLimitService = rateLimitService;
        // Disable rate limiting in test profile
        this.rateLimitEnabled = !Arrays.asList(environment.getActiveProfiles()).contains("test");
        if (!rateLimitEnabled) {
            log.info("Rate limiting is DISABLED for test profile");
        }
    }

    @Around("@annotation(rateLimit)")
    public Object checkRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        // Skip rate limiting if disabled (e.g., in test profile)
        if (!rateLimitEnabled) {
            return joinPoint.proceed();
        }

        // Build rate limit key
        String key = buildRateLimitKey(joinPoint, rateLimit);

        // Calculate window duration
        Duration window = Duration.of(rateLimit.window(), rateLimit.unit());

        // Check if request is allowed
        boolean allowed = rateLimitService.allow(key, rateLimit.limit(), window);

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error", "RATE_LIMIT_EXCEEDED",
                            "message", String.format("Too many requests. Limit: %d requests per %d %s",
                                    rateLimit.limit(), rateLimit.window(), rateLimit.unit().name().toLowerCase()),
                            "retryAfter", window.getSeconds()
                    ));
        }

        // Proceed with the method execution
        return joinPoint.proceed();
    }

    /**
     * Build unique key for rate limiting
     * Format: {keyPrefix}:{methodName}:{userId|ip}
     */
    private String buildRateLimitKey(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();

        // Use custom key prefix if provided, otherwise use method name
        String prefix = rateLimit.keyPrefix().isEmpty() ? methodName : rateLimit.keyPrefix();

        // Get user identifier (userId or IP address)
        String identifier = getUserIdentifier();

        return prefix + ":" + identifier;
    }

    /**
     * Get user identifier for rate limiting
     * Uses authenticated user ID if available, otherwise uses IP address
     */
    private String getUserIdentifier() {
        // Try to get authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        // Fall back to IP address
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = getClientIP(request);
            return "ip:" + ip;
        }

        return "unknown";
    }

    /**
     * Get client IP address, considering proxies
     */
    private String getClientIP(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_X_FORWARDED",
                "HTTP_X_CLUSTER_CLIENT_IP",
                "HTTP_CLIENT_IP",
                "HTTP_FORWARDED_FOR",
                "HTTP_FORWARDED",
                "HTTP_VIA",
                "REMOTE_ADDR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For may contain multiple IPs, take the first one
                if (ip.contains(",")) {
                    ip = ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }
}
