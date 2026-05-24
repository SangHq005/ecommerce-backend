package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByIdAndSellerUserId(Long id, Long sellerUserId);
    Optional<ProductEntity> findBySlug(String slug);
    List<ProductEntity> findBySellerUserIdOrderByIdDesc(Long sellerUserId);
    Page<ProductEntity> findByStatusAndCategoryIdAndBrandId(String status, Long categoryId, Long brandId, Pageable pageable);
    Page<ProductEntity> findByStatus(String status, Pageable pageable);
    Page<ProductEntity> findByShopIdAndSellerUserId(Long shopId, Long sellerUserId, Pageable pageable);
    List<ProductEntity> findByStatus(String status);
    List<ProductEntity> findByStatusAndCategoryId(String status, Long categoryId);
    List<ProductEntity> findByStatusAndBrandId(String status, Long brandId);
    List<ProductEntity> findByCategoryIdAndStatus(Long categoryId, String status, Pageable pageable);
    long countByStatus(String status);
    long countByCategoryId(Long categoryId);
    long countByShopIdAndStatus(Long shopId, String status);
    long countByShopIdAndSellerUserId(Long shopId, Long sellerUserId);

    @Query("SELECT p.categoryId, COUNT(p) FROM ProductEntity p GROUP BY p.categoryId")
    List<Object[]> countProductsByCategory();

    Page<ProductEntity> findBySellerUserIdOrderByIdDesc(Long sellerUserId, Pageable pageable);

    Page<ProductEntity> findBySellerUserIdAndStatusOrderByIdDesc(Long sellerUserId, String status, Pageable pageable);
    
    // Admin search methods
    Page<ProductEntity> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<ProductEntity> findByStatusAndNameContainingIgnoreCase(String status, String name, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM ProductEntity p WHERE p.status = 'ACTIVE' AND p.stockQuantity > 0 AND p.originalPrice IS NOT NULL AND p.originalPrice > p.price ORDER BY ((p.originalPrice - p.price) * 1.0 / p.originalPrice) DESC")
    List<ProductEntity> findTopDeals(Pageable pageable);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE ProductEntity p SET p.soldCount = p.soldCount + :quantity WHERE p.id = :productId")
    void incrementSoldCount(@org.springframework.data.repository.query.Param("productId") Long productId, @org.springframework.data.repository.query.Param("quantity") int quantity);


    List<ProductEntity> findByShopIdAndStatus(Long shopId, String status);
    List<ProductEntity> findByMainImageUrlStartingWith(String prefix);
}
