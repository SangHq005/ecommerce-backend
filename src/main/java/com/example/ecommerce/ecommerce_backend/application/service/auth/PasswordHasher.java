package com.example.ecommerce.ecommerce_backend.application.service.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String raw) { return encoder.encode(raw); }
    public boolean matches(String raw, String hashed) {
        if (hashed == null) {
            return false;
        }
        if (!hashed.startsWith("$2") && hashed.equals(raw)) {
            return true;
        }
        return encoder.matches(raw, hashed);
    }
}
