package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.RoleEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.JoinType;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AdminUserService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("ACTIVE", "DISABLED");

    private final UserJpaRepository userRepo;
    private final RoleJpaRepository roleRepo;

    public AdminUserService(UserJpaRepository userRepo, RoleJpaRepository roleRepo) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
    }

    @Transactional(readOnly = true)
    public Page<UserEntity> search(String q, String status, String role, Pageable pageable) {
        Specification<UserEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();

            if (status != null && !status.isBlank()) {
                predicates.getExpressions().add(cb.equal(root.get("status"), status.toUpperCase(Locale.ROOT)));
            }

            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.getExpressions().add(cb.or(
                        cb.like(cb.lower(root.get("email")), like),
                        cb.like(cb.lower(root.get("fullName")), like)
                ));
            }

            if (role != null && !role.isBlank()) {
                var roles = root.join("roles", JoinType.LEFT);
                predicates.getExpressions().add(cb.equal(roles.get("code"), role.toUpperCase(Locale.ROOT)));
                query.distinct(true);
            }

            return predicates;
        };

        return userRepo.findAll(spec, pageable);
    }

    @Transactional
    public UserEntity updateStatus(Long userId, String status) {
        String normalized = status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw ApiException.badRequest("Invalid status");
        }

        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));
        user.setStatus(normalized);
        user.setUpdatedAt(Instant.now());
        return userRepo.save(user);
    }

    @Transactional
    public UserEntity updateRoles(Long userId, List<String> roleCodes) {
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User not found"));

        Set<RoleEntity> roles = new HashSet<>();
        for (String code : roleCodes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String normalized = code.trim().toUpperCase(Locale.ROOT);
            RoleEntity role = roleRepo.findByCode(normalized)
                    .orElseThrow(() -> ApiException.badRequest("Role not found: " + normalized));
            roles.add(role);
        }

        user.setRoles(roles);
        user.setUpdatedAt(Instant.now());
        return userRepo.save(user);
    }
}
