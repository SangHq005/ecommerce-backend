package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageJpaRepository extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findByProductIdOrderBySortOrderAsc(Long productId);
    Optional<ProductImageEntity> findByProductIdAndSortOrder(Long productId, int sortOrder);
}