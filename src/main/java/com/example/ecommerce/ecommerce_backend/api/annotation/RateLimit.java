package com.example.ecommerce.ecommerce_backend.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Rate limiting annotation for controller methods
 * Apply this annotation to limit the number of requests per time window
 *
 * Example:
 * @RateLimit(limit = 5, window = 15, unit = ChronoUnit.MINUTES)
 * public ResponseEntity<?> login() { ... }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum number of requests allowed in the time window
     */
    int limit() default 100;

    /**
     * Time window duration
     */
    int window() default 1;

    /**
     * Time unit for the window (MINUTES, HOURS, etc.)
     */
    ChronoUnit unit() default ChronoUnit.MINUTES;

    /**
     * Key prefix for rate limit (default uses method name + user)
     * Use custom key for specific rate limiting strategies
     */
    String keyPrefix() default "";
}
