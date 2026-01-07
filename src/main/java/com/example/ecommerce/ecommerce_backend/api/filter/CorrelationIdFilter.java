package com.example.ecommerce.ecommerce_backend.api.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String MDC_KEY = "correlationId";

    private final String headerName;

    public CorrelationIdFilter(@Value("${app.observability.correlation-id-header:X-Correlation-Id}") String headerName) {
        this.headerName = headerName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String incoming = Optional.ofNullable(request.getHeader(headerName)).orElse(null);
        String correlationId = (incoming == null || incoming.isBlank())
                ? "c-" + UUID.randomUUID()
                : incoming;

        MDC.put(MDC_KEY, correlationId);
        response.setHeader(headerName, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
