package com.example.ecommerce.ecommerce_backend.application.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordHasher {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String raw) { return encoder.encode(raw); }
    public boolean matches(String raw, String hashed) { return encoder.matches(raw, hashed); }
}
