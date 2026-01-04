package com.example.ecommerce.ecommerce_backend.api.dto.profile;

import com.example.ecommerce.ecommerce_backend.domain.model.Gender;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record UpdateProfileRequest(
        @Pattern(regexp="^[0-9+\\- ]{8,32}$", message="invalid phone") String phone,
        Gender gender,
        LocalDate dateOfBirth
) {}
