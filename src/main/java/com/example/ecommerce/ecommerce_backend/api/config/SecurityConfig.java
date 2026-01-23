package com.example.ecommerce.ecommerce_backend.api.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.example.ecommerce.ecommerce_backend.api.filter.JwtAuthFilter;
import com.example.ecommerce.ecommerce_backend.api.security.OAuth2SuccessHandler;
import com.example.ecommerce.ecommerce_backend.infrastructure.config.CorsProperties;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final Environment environment;

    public SecurityConfig(CorsProperties corsProperties, Environment environment) {
        this.corsProperties = corsProperties;
        this.environment = environment;
    }

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
                .requestMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh", "/api/v1/auth/trusted-login").permitAll()
                .requestMatchers("/api/v1/auth/otp/**").permitAll()
                .requestMatchers("/api/v1/auth/forgot-password", "/api/v1/auth/reset-password").permitAll()
                .requestMatchers("/oauth2/**", "/login/oauth2/**", "/oauth2/authorization/**").permitAll()

                // Public endpoints - Catalog & Products (Read-only)
                .requestMatchers(HttpMethod.GET, "/api/v1/catalog/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/brands/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/recommendations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/vouchers/shop/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/products/*/reviews").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/compare/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll() // Product detail with variants

                // Public endpoints - AI Assistant (Public access for all users)
                .requestMatchers("/api/v1/ai-assistant/**").permitAll()

                // Public endpoints - Payment callbacks
                .requestMatchers("/api/v1/payment/*/callback", "/api/v1/payment/vnpay/callback").permitAll()

                // Public endpoints - Static files
                .requestMatchers("/uploads/**").permitAll()

                // Debug endpoints - only available in dev profile
                .requestMatchers(HttpMethod.GET, "/api/v1/_debug/**").access((authentication, ctx) -> {
                    boolean isDev = Arrays.asList(environment.getActiveProfiles()).contains("dev");
                    return new org.springframework.security.authorization.AuthorizationDecision(isDev);
                })

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
     * CORS configuration to allow frontend applications to access the API.
     * Configuration is externalized via CorsProperties (app.cors.* properties).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use externalized configuration
        configuration.setAllowedOrigins(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(corsProperties.getAllowedMethods());
        configuration.setAllowedHeaders(corsProperties.getAllowedHeaders());
        configuration.setAllowCredentials(corsProperties.isAllowCredentials());
        configuration.setMaxAge(corsProperties.getMaxAge());
        configuration.setExposedHeaders(corsProperties.getExposedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
