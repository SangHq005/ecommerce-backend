package com.example.ecommerce.ecommerce_backend.api.security;

import com.example.ecommerce.ecommerce_backend.application.service.auth.AuthService;
import com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.security.oauth2.success-redirect-url}")
    private String redirectUrl;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        try {
            DefaultOAuth2User user = (DefaultOAuth2User) authentication.getPrincipal();

            String sub = String.valueOf(user.getAttributes().get("sub"));
            String email = String.valueOf(user.getAttributes().get("email"));
            String name = String.valueOf(user.getAttributes().getOrDefault("name", email));

            JwtService.TokenPair pair = authService.oauth2LoginOrLink(
                    sub, email, name, request.getHeader("User-Agent"), request.getRemoteAddr()
            );

            String target = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("accessToken", pair.accessToken())
                    .queryParam("refreshToken", pair.refreshToken())
                    .build().toUriString();

            response.sendRedirect(target);
        } catch (IllegalArgumentException e) {
            // Handle business logic errors (e.g., user disabled)
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Authentication failed";
            }
            String target = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("error", "oauth2_failed")
                    .queryParam("message", errorMessage)
                    .queryParam("error_description", errorMessage) // Also include for compatibility
                    .build().toUriString();
            response.sendRedirect(target);
        } catch (Exception e) {
            // Handle other unexpected errors
            String errorMessage = "An unexpected error occurred during authentication";
            if (e.getMessage() != null && !e.getMessage().isEmpty()) {
                errorMessage = e.getMessage();
            }
            String target = UriComponentsBuilder.fromUriString(redirectUrl)
                    .queryParam("error", "oauth2_failed")
                    .queryParam("message", errorMessage)
                    .queryParam("error_description", errorMessage) // Also include for compatibility
                    .build().toUriString();
            response.sendRedirect(target);
        }
    }
}
