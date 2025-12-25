package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsageEntity, Long> {
    List<CouponUsageEntity> findByCouponId(Long couponId);
    List<CouponUsageEntity> findByUserId(Long userId);
    long countByCouponId(Long couponId);
    long countByCouponIdAndUserId(Long couponId, Long userId);
    boolean existsByOrderId(Long orderId);
}
