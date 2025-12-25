package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByIdAndSellerUserId(Long id, Long sellerUserId);
    Page<ProductEntity> findByStatusAndCategoryIdAndBrandId(String status, Long categoryId, Long brandId, Pageable pageable);
    Page<ProductEntity> findByStatus(String status, Pageable pageable);
    Page<ProductEntity> findByShopIdAndSellerUserId(Long shopId, Long sellerUserId, Pageable pageable);
    List<ProductEntity> findByStatus(String status);
    List<ProductEntity> findByStatusAndCategoryId(String status, Long categoryId);
    List<ProductEntity> findByStatusAndBrandId(String status, Long brandId);
    List<ProductEntity> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
}
