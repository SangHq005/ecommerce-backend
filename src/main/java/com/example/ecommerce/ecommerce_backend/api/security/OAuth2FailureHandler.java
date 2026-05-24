package com.example.ecommerce.ecommerce_backend.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.security.oauth2.success-redirect-url}")
    private String redirectUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        // Redirect to frontend with error message
        String errorMessage = exception.getMessage();
        if (errorMessage == null || errorMessage.isEmpty()) {
            errorMessage = "OAuth2 authentication failed. Please try again.";
        }
        
        String target = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("error", "oauth2_failed")
                .queryParam("message", errorMessage)
                .queryParam("error_description", errorMessage) // Also include for compatibility
                .build().toUriString();

        response.sendRedirect(target);
    }
}
