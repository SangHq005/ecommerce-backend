package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for Vietnamese phone numbers
 * Accepts formats:
 * - +84xxxxxxxxx (10 digits after +84)
 * - 0xxxxxxxxx (10 digits starting with 0)
 * - 84xxxxxxxxx (10 digits after 84)
 */
public class PhoneValidator implements ConstraintValidator<ValidPhone, String> {

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)(3|5|7|8|9)\\d{8}$"
    );

    @Override
    public boolean isValid(String phone, ConstraintValidatorContext context) {
        if (phone == null || phone.isBlank()) {
            return true; // Use @NotBlank for null checks
        }

        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }
}
