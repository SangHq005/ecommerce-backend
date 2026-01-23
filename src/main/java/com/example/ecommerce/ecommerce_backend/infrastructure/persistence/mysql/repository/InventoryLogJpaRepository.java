package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.InventoryLogEntity;

public interface InventoryLogJpaRepository extends JpaRepository<InventoryLogEntity, Long> {
    Page<InventoryLogEntity> findBySkuId(Long skuId, Pageable pageable);
    Page<InventoryLogEntity> findByShopId(Long shopId, Pageable pageable);
    List<InventoryLogEntity> findByReferenceId(String referenceId);
    
    // NEW: For reports
    List<InventoryLogEntity> findByShopIdAndCreatedAtAfter(Long shopId, Instant since);
    List<InventoryLogEntity> findByProductIdAndCreatedAtAfter(Long productId, Instant since);
}
