package com.example.ecommerce.ecommerce_backend.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Custom validation annotation for Vietnamese phone numbers
 * Validates format: +84xxxxxxxxx or 0xxxxxxxxx (10-11 digits)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
@Documented
public @interface ValidPhone {
    String message() default "Invalid phone number format. Expected Vietnamese phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
