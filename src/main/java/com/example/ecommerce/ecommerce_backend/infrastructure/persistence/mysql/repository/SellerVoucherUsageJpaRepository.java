package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherUsageEntity;

@Repository
public interface SellerVoucherUsageJpaRepository extends JpaRepository<SellerVoucherUsageEntity, Long> {

    // Find usage by voucher
    List<SellerVoucherUsageEntity> findByVoucherId(Long voucherId);

    // Find usage by user
    List<SellerVoucherUsageEntity> findByUserId(Long userId);

    // Find usage by order
    Optional<SellerVoucherUsageEntity> findByOrderId(Long orderId);

    // Count user usage for a voucher
    long countByVoucherIdAndUserId(Long voucherId, Long userId);

    // Check if user already used voucher
    boolean existsByVoucherIdAndUserId(Long voucherId, Long userId);

    // Get total discount given by a voucher
    @Query("SELECT COALESCE(SUM(u.discountAmount), 0) FROM SellerVoucherUsageEntity u WHERE u.voucherId = :voucherId")
    Long getTotalDiscountByVoucher(@Param("voucherId") Long voucherId);

    // Get user's voucher usage history
    @Query("SELECT u FROM SellerVoucherUsageEntity u WHERE u.userId = :userId ORDER BY u.usedAt DESC")
    List<SellerVoucherUsageEntity> findUserUsageHistory(@Param("userId") Long userId);
}
