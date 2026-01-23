package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerVoucherEntity.VoucherStatus;

@Repository
public interface SellerVoucherJpaRepository extends JpaRepository<SellerVoucherEntity, Long> {

    // Find by shop
    List<SellerVoucherEntity> findByShopId(Long shopId);
    
    Page<SellerVoucherEntity> findByShopId(Long shopId, Pageable pageable);

    // Find by shop and status
    List<SellerVoucherEntity> findByShopIdAndStatus(Long shopId, VoucherStatus status);
    
    Page<SellerVoucherEntity> findByShopIdAndStatus(Long shopId, VoucherStatus status, Pageable pageable);

    // Find by code within shop
    Optional<SellerVoucherEntity> findByShopIdAndCode(Long shopId, String code);

    // Find by code (across all shops for validation)
    Optional<SellerVoucherEntity> findByCode(String code);

    // Find active vouchers for a shop
    @Query("SELECT v FROM SellerVoucherEntity v WHERE v.shopId = :shopId " +
           "AND v.status = 'ACTIVE' " +
           "AND v.startDate <= :now AND v.endDate > :now " +
           "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    List<SellerVoucherEntity> findActiveVouchersByShop(@Param("shopId") Long shopId, @Param("now") Instant now);

    // Find valid voucher by code for a specific shop
    @Query("SELECT v FROM SellerVoucherEntity v WHERE v.code = :code " +
           "AND v.shopId = :shopId " +
           "AND v.status = 'ACTIVE' " +
           "AND v.startDate <= :now AND v.endDate > :now " +
           "AND (v.usageLimit IS NULL OR v.usageCount < v.usageLimit)")
    Optional<SellerVoucherEntity> findValidVoucherByCodeAndShop(
            @Param("code") String code,
            @Param("shopId") Long shopId,
            @Param("now") Instant now);

    // Check if code exists for shop
    boolean existsByShopIdAndCode(Long shopId, String code);

    // Find expired vouchers that need status update
    @Query("SELECT v FROM SellerVoucherEntity v WHERE v.status = 'ACTIVE' AND v.endDate < :now")
    List<SellerVoucherEntity> findExpiredVouchers(@Param("now") Instant now);

    // Count vouchers by shop and status
    long countByShopIdAndStatus(Long shopId, VoucherStatus status);

    // Search vouchers
    @Query("SELECT v FROM SellerVoucherEntity v WHERE v.shopId = :shopId " +
           "AND (LOWER(v.code) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SellerVoucherEntity> searchByShop(
            @Param("shopId") Long shopId,
            @Param("keyword") String keyword,
            Pageable pageable);
}
