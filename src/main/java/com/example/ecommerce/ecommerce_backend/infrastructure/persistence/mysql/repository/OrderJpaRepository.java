package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);

    // Seller queries
    Page<OrderEntity> findByShopId(Long shopId, Pageable pageable);
    Page<OrderEntity> findByShopIdAndStatus(Long shopId, String status, Pageable pageable);
    List<OrderEntity> findByShopIdAndStatus(Long shopId, String status);
    long countByShopIdAndStatus(Long shopId, String status);

    @Query("select o from OrderEntity o where o.shopId = :shopId and o.createdAt between :startDate and :endDate")
    List<OrderEntity> findByShopIdAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // User queries
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);
    List<OrderEntity> findByUserIdAndStatus(Long userId, String status);
}
