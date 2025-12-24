package com.example.ecommerce.ecommerce_backend.api.config;

import com.example.ecommerce.ecommerce_backend.api.filter.JwtAuthFilter;
import com.example.ecommerce.ecommerce_backend.api.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthFilter jwtAuthFilter,
            OAuth2SuccessHandler oAuth2SuccessHandler
    ) throws Exception {

        // CSRF disabled because we're using stateless JWT authentication
        // For JWT-based APIs, CSRF protection is not required as tokens are stored in headers
        http.csrf(csrf -> csrf.disable());

        // Enable CORS with custom configuration
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // Stateless session - no session stored on server
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // Custom exception handlers
        http.exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> res.sendError(401, "Unauthorized"))
                .accessDeniedHandler((req, res, ex) -> res.sendError(403, "Forbidden"))
        );

        // Authorization rules
        http.authorizeHttpRequests(auth -> auth
                // Public endpoints - Health & Documentation
                .requestMatchers("/", "/error").permitAll()
                .requestMatchers("/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                // Public endpoints - Authentication
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                // Public endpoints - Catalog & Products (Read-only)
                .requestMatchers(HttpMethod.GET, "/api/v1/catalog/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()

                // Public endpoints - Payment callbacks
                .requestMatchers("/api/v1/payment/*/callback", "/api/v1/payment/vnpay/callback").permitAll()

                // Public endpoints - Static files
                .requestMatchers("/uploads/**").permitAll()

                // Debug endpoints (should be removed in production)
                .requestMatchers(HttpMethod.GET, "/api/v1/_debug/ping").permitAll()

                // Admin endpoints - require ADMIN role
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // All other endpoints require authentication
                .anyRequest().authenticated()
        );

        // OAuth2 login configuration
        http.oauth2Login(o -> o.successHandler(oAuth2SuccessHandler));

        // Add JWT authentication filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS configuration to allow frontend applications to access the API
     * Customize allowed origins based on your deployment environment
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow requests from frontend applications
        // TODO: Update these URLs based on your actual frontend deployment
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",      // React/Next.js development
                "http://localhost:4200",      // Angular development
                "http://localhost:5173",      // Vite development
                "http://localhost:8081"       // Alternative frontend port
                // Add production frontend URLs here
        ));

        // Allow common HTTP methods
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        // Allow all headers (you can restrict this in production)
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers, etc.)
        configuration.setAllowCredentials(true);

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Expose headers that frontend can access
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "X-Correlation-ID",
                "X-Total-Count"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
