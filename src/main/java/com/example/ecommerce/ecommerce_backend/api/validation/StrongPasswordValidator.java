package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for strong password requirements
 */
public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?]");

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false; // Let @NotBlank handle this
        }

        boolean valid = true;
        StringBuilder message = new StringBuilder();

        if (password.length() < MIN_LENGTH) {
            message.append("Password must be at least ").append(MIN_LENGTH).append(" characters. ");
            valid = false;
        }

        if (!UPPERCASE.matcher(password).find()) {
            message.append("Password must contain at least one uppercase letter. ");
            valid = false;
        }

        if (!LOWERCASE.matcher(password).find()) {
            message.append("Password must contain at least one lowercase letter. ");
            valid = false;
        }

        if (!DIGIT.matcher(password).find()) {
            message.append("Password must contain at least one digit. ");
            valid = false;
        }

        if (!SPECIAL.matcher(password).find()) {
            message.append("Password must contain at least one special character (!@#$%^&*). ");
            valid = false;
        }

        if (!valid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(message.toString().trim())
                    .addConstraintViolation();
        }

        return valid;
    }
}
