package com.example.ecommerce.ecommerce_backend.application.service.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class RequestHash {
    private RequestHash() {}

    public static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
