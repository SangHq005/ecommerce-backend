package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SkuJpaRepository extends JpaRepository<SkuEntity, Long> {
    List<SkuEntity> findByProductIdOrderByIdAsc(Long productId);
    Optional<SkuEntity> findByProductIdAndOptionSignatureHash(Long productId, String optionSignatureHash);
}