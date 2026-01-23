package com.example.ecommerce.ecommerce_backend.api.dto.admin;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;

import java.time.Instant;
import java.util.List;

public record AdminUserResponse(
        Long id,
        String email,
        String fullName,
        String status,
        List<String> roles,
        Instant createdAt,
        Instant updatedAt
) {
    public static AdminUserResponse from(UserEntity user) {
        List<String> roleCodes = user.getRoles().stream()
                .map(RoleEntity::getCode)
                .sorted()
                .toList();
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                roleCodes,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
