package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;

public interface OrderJpaRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderCode(String orderCode);

    // Seller queries
    Page<OrderEntity> findByShopId(Long shopId, Pageable pageable);
    Page<OrderEntity> findByShopIdAndStatus(Long shopId, String status, Pageable pageable);
    List<OrderEntity> findByShopIdAndStatus(Long shopId, String status);
    long countByShopIdAndStatus(Long shopId, String status);

    long countByCreatedAtAfter(LocalDateTime start);
    long countByStatus(String status);
    long countByStatusIn(List<String> statuses);

    List<OrderEntity> findByStatusInAndCreatedAtAfter(List<String> statuses, LocalDateTime start);

    List<OrderEntity> findTop10ByOrderByCreatedAtDesc();

    @Query("select coalesce(sum(o.totalAmount), 0) from OrderEntity o where o.status in :statuses")
    Long sumTotalAmountByStatusIn(@Param("statuses") List<String> statuses);

    @Query("select coalesce(sum(o.totalAmount), 0) from OrderEntity o where o.status in :statuses and o.createdAt >= :start")
    Long sumTotalAmountByStatusInAndCreatedAtAfter(@Param("statuses") List<String> statuses,
                                                   @Param("start") LocalDateTime start);

    @Query("select coalesce(sum(o.totalAmount), 0) from OrderEntity o " +
            "where o.status in :statuses and o.createdAt >= :start and o.createdAt < :end")
    Long sumTotalAmountByStatusInAndCreatedAtBetween(@Param("statuses") List<String> statuses,
                                                     @Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

    @Query("select o from OrderEntity o where o.shopId = :shopId and o.createdAt between :startDate and :endDate")
    List<OrderEntity> findByShopIdAndDateRange(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // User queries
    Page<OrderEntity> findByUserId(Long userId, Pageable pageable);
    List<OrderEntity> findByUserIdAndStatus(Long userId, String status);
    
    // === NEW: Auto-complete queries ===
    
    /**
     * Find orders ready for auto-completion
     * Orders that are DELIVERED and auto_complete_at has passed
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.autoCompleteAt <= :now AND o.buyerConfirmed = false")
    List<OrderEntity> findOrdersToAutoComplete(
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );
    
    /**
     * Find delivered orders that need reminder
     * Orders delivered between autoCompleteThreshold and reminderThreshold ago
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status " +
           "AND o.deliveredAt > :autoCompleteThreshold " +
           "AND o.deliveredAt <= :reminderThreshold " +
           "AND o.buyerConfirmed = false")
    List<OrderEntity> findDeliveredOrdersNeedingReminder(
            @Param("status") String status,
            @Param("autoCompleteThreshold") LocalDateTime autoCompleteThreshold,
            @Param("reminderThreshold") LocalDateTime reminderThreshold
    );
    
    /**
     * Find orders by status with shipping info
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.shippingProvider = :provider")
    List<OrderEntity> findByStatusAndShippingProvider(
            @Param("status") String status,
            @Param("provider") String provider
    );
    
    /**
     * Count orders by status for a shop within date range
     */
    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.shopId = :shopId AND o.status = :status " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countByShopIdAndStatusAndDateRange(
            @Param("shopId") Long shopId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Find orders pending delivery confirmation (for seller dashboard)
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.shopId = :shopId AND o.status = :status " +
           "AND o.buyerConfirmed = false ORDER BY o.deliveredAt ASC")
    List<OrderEntity> findPendingConfirmationOrders(
            @Param("shopId") Long shopId,
            @Param("status") String status
    );
}
