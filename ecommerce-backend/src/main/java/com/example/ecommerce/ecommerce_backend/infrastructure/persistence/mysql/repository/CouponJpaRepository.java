package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {
    Optional<CouponEntity> findByCode(String code);
    Optional<CouponEntity> findByCodeAndStatus(String code, CouponStatus status);
    List<CouponEntity> findByStatus(CouponStatus status);
    List<CouponEntity> findByAutoApplyTrue();

    @Query("select c from CouponEntity c where c.status = 'ACTIVE' and c.startDate <= :now and c.endDate >= :now")
    List<CouponEntity> findActiveValidCoupons(@Param("now") Instant now);

    @Query("select c from CouponEntity c where c.autoApply = true and c.status = 'ACTIVE' and c.startDate <= :now and c.endDate >= :now")
    List<CouponEntity> findAutoApplyCoupons(@Param("now") Instant now);
}
