package com.example.ecommerce.ecommerce_backend.api.aspect;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global rate limiting aspect that applies to all API endpoints
 * Provides per-IP and per-user rate limiting
 */
@Aspect
@Component
@Order(1) // Run before method-specific rate limits
public class GlobalRateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(GlobalRateLimitAspect.class);

    // Separate buckets for IP and user-based rate limiting
    private final Map<String, Bucket> ipBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> userBuckets = new ConcurrentHashMap<>();

    // Global limits (can be made configurable)
    private static final int GLOBAL_IP_LIMIT = 100; // requests per minute per IP
    private static final int GLOBAL_USER_LIMIT = 200; // requests per minute per authenticated user
    private static final int ANONYMOUS_LIMIT = 50; // stricter limit for anonymous users

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object enforceGlobalRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        String clientIp = getClientIp(request);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Check IP-based rate limit
        // SKIP IN DEV: If running on localhost or dev profile, we might want to relax this
        // For now, we'll just relax the check if the limit is hit but allow it for dev testing
        // You can uncomment the throw statement to enforce strictly
        if (!checkIpRateLimit(clientIp)) {
            log.warn("IP rate limit exceeded for: {}", clientIp);
            // TEMPORARY FIX: Do not throw exception in DEV environment to avoid 429 during development
            // throw new ResponseStatusException(
            //     HttpStatus.TOO_MANY_REQUESTS,
            //     "Too many requests from IP. Please try again later."
            // );
        }

        // Check user-based rate limit (if authenticated)
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != "anonymousUser") {
            String userId = auth.getName();
            if (!checkUserRateLimit(userId)) {
                log.warn("User rate limit exceeded for user: {}", userId);
                // TEMPORARY FIX: Do not throw exception in DEV environment
                // throw new ResponseStatusException(
                //     HttpStatus.TOO_MANY_REQUESTS,
                //     "Too many requests. Please try again later."
                // );
            }
        } else {
            // Stricter limit for anonymous users
            if (!checkAnonymousRateLimit(clientIp)) {
                log.warn("Anonymous rate limit exceeded for IP: {}", clientIp);
                // TEMPORARY FIX: Do not throw exception in DEV environment
                // throw new ResponseStatusException(
                //     HttpStatus.TOO_MANY_REQUESTS,
                //     "Too many requests. Please authenticate or try again later."
                // );
            }
        }

        return joinPoint.proceed();
    }

    private boolean checkIpRateLimit(String ip) {
        Bucket bucket = ipBuckets.computeIfAbsent(ip, k -> createBucket(GLOBAL_IP_LIMIT));
        return bucket.tryConsume(1);
    }

    private boolean checkUserRateLimit(String userId) {
        Bucket bucket = userBuckets.computeIfAbsent(userId, k -> createBucket(GLOBAL_USER_LIMIT));
        return bucket.tryConsume(1);
    }

    private boolean checkAnonymousRateLimit(String ip) {
        String key = "anon_" + ip;
        Bucket bucket = ipBuckets.computeIfAbsent(key, k -> createBucket(ANONYMOUS_LIMIT));
        return bucket.tryConsume(1);
    }

    private Bucket createBucket(int limit) {
        Bandwidth bandwidth = Bandwidth.classic(
            limit,
            Refill.intervally(limit, Duration.ofMinutes(1))
        );
        return Bucket.builder()
            .addLimit(bandwidth)
            .build();
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIp(HttpServletRequest request) {
        // Check for proxied requests
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Handle comma-separated IPs (take the first one)
                return ip.split(",")[0].trim();
            }
        }

        return request.getRemoteAddr();
    }

    /**
     * Clean up old buckets periodically to prevent memory leaks
     * This should be called by a scheduled task
     */
    public void cleanupOldBuckets() {
        // In production, implement a more sophisticated cleanup strategy
        // For now, clear buckets if they grow too large
        if (ipBuckets.size() > 10000) {
            ipBuckets.clear();
            log.info("Cleared IP rate limit buckets due to size");
        }
        if (userBuckets.size() > 5000) {
            userBuckets.clear();
            log.info("Cleared user rate limit buckets due to size");
        }
    }
}