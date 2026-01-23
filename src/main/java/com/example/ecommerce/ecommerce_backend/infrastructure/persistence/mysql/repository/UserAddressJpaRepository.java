package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressJpaRepository extends JpaRepository<UserAddressEntity, Long> {
    List<UserAddressEntity> findByUserIdOrderByIsDefaultDescIdDesc(Long userId);
    Optional<UserAddressEntity> findByIdAndUserId(Long id, Long userId);
    boolean existsByIdAndUserId(Long id, Long userId);
    List<UserAddressEntity> findByUserIdAndIsDefaultTrue(Long userId);
    long countByUserId(Long userId);
}
