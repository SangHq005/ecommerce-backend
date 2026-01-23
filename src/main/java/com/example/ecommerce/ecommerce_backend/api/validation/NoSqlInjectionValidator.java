package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator to check for potential SQL injection patterns
 */
public class NoSqlInjectionValidator implements ConstraintValidator<NoSqlInjection, String> {

    // Common SQL injection patterns
    private static final Pattern[] DANGEROUS_PATTERNS = {
        Pattern.compile(".*([';]+|(--)+).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*(union|select|insert|update|delete|drop|create|alter|exec|execute|script|javascript).*",
                        Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*(<script|<iframe|<object|<embed|<img).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*(xp_|sp_|0x).*", Pattern.CASE_INSENSITIVE),
        Pattern.compile(".*(\\bor\\b|\\band\\b)\\s*['\"]?\\s*[0-9]+\\s*=\\s*[0-9]+.*", Pattern.CASE_INSENSITIVE)
    };

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let other validators handle null/blank
        }

        // Check against dangerous patterns
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(value).matches()) {
                return false;
            }
        }

        return true;
    }
}