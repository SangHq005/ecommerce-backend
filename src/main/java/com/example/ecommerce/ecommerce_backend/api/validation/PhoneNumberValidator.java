package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator for phone number format
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {

    // Vietnam phone number pattern (default)
    private static final Pattern VN_PHONE = Pattern.compile("^(\\+84|84|0)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])[0-9]{7}$");

    // International phone pattern (E.164 format)
    private static final Pattern INTL_PHONE = Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private boolean allowInternational;

    @Override
    public void initialize(PhoneNumber constraintAnnotation) {
        this.allowInternational = constraintAnnotation.international();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return true; // Let @NotBlank handle this
        }

        // Remove spaces and dashes
        phoneNumber = phoneNumber.replaceAll("[\\s-]", "");

        // Check Vietnam format first
        if (VN_PHONE.matcher(phoneNumber).matches()) {
            return true;
        }

        // Check international format if allowed
        if (allowInternational && INTL_PHONE.matcher(phoneNumber).matches()) {
            return true;
        }

        return false;
    }
}