package com.example.ecommerce.ecommerce_backend.api.dto.profile;

import com.example.ecommerce.ecommerce_backend.domain.model.Gender;

import java.time.LocalDate;

public record ProfileResponse(
        Long userId,
        String phone,
        Gender gender,
        LocalDate dateOfBirth,
        String avatarUrl
) {}
