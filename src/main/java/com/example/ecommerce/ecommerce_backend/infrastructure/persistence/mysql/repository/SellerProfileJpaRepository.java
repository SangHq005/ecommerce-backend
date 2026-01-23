package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerProfileEntity.SellerStatus;

public interface SellerProfileJpaRepository extends JpaRepository<SellerProfileEntity, Long> {
    
    Optional<SellerProfileEntity> findByUserId(Long userId);
    
    boolean existsByUserId(Long userId);
    
    List<SellerProfileEntity> findByStatus(SellerStatus status);
    
    Page<SellerProfileEntity> findByStatus(SellerStatus status, Pageable pageable);
    
    long countByStatus(SellerStatus status);
}
