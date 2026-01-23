package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductStatusHistoryEntity;

public interface ProductStatusHistoryJpaRepository extends JpaRepository<ProductStatusHistoryEntity, Long> {
    
    List<ProductStatusHistoryEntity> findByProductIdOrderByCreatedAtDesc(Long productId);
    
    Page<ProductStatusHistoryEntity> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable pageable);
    
    List<ProductStatusHistoryEntity> findByProductIdAndNewStatusOrderByCreatedAtDesc(Long productId, String status);
}
