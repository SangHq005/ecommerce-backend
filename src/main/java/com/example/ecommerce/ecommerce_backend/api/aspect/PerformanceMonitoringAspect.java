package com.example.ecommerce.ecommerce_backend.api.aspect;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Aspect for monitoring performance of service methods and controllers
 */
@Aspect
@Component
public class PerformanceMonitoringAspect {

    private static final Logger log = LoggerFactory.getLogger(PerformanceMonitoringAspect.class);

    private final MeterRegistry meterRegistry;
    private static final long SLOW_METHOD_THRESHOLD_MS = 1000;

    @Autowired(required = false)
    public PerformanceMonitoringAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Around("@within(org.springframework.web.bind.annotation.RestController) || " +
            "@within(org.springframework.stereotype.Service)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = method.getName();
        String metricName = className + "." + methodName;

        Timer.Sample sample = null;
        if (meterRegistry != null) {
            sample = Timer.start(meterRegistry);
        }

        try {
            // Execute the method
            Object result = joinPoint.proceed();

            // Record success
            recordMetric(sample, metricName, "success");

            return result;

        } catch (Exception e) {
            // Record failure
            recordMetric(sample, metricName, "failure");

            // Log error
            log.error("Method {} failed with exception: {}", metricName, e.getMessage());

            throw e;

        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // Log slow methods
            if (executionTime > SLOW_METHOD_THRESHOLD_MS) {
                log.warn("Slow method execution: {} took {}ms", metricName, executionTime);
            } else if (log.isDebugEnabled()) {
                log.debug("Method {} executed in {}ms", metricName, executionTime);
            }

            // Record execution time metric
            if (meterRegistry != null) {
                meterRegistry.gauge("method.execution.time", executionTime);
            }
        }
    }

    private void recordMetric(Timer.Sample sample, String metricName, String status) {
        if (sample != null && meterRegistry != null) {
            sample.stop(Timer.builder("method.execution")
                    .tag("method", metricName)
                    .tag("status", status)
                    .register(meterRegistry));
        }
    }
}