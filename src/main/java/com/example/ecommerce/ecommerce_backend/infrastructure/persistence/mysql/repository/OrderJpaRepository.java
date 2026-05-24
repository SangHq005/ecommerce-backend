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
    
    // Admin queries
    Page<OrderEntity> findByStatus(String status, Pageable pageable);

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
        
    
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.autoCompleteAt <= :now AND o.buyerConfirmed = false")
    List<OrderEntity> findOrdersToAutoComplete(
            @Param("status") String status,
            @Param("now") LocalDateTime now
    );
    
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status " +
           "AND o.deliveredAt > :autoCompleteThreshold " +
           "AND o.deliveredAt <= :reminderThreshold " +
           "AND o.buyerConfirmed = false")
    List<OrderEntity> findDeliveredOrdersNeedingReminder(
            @Param("status") String status,
            @Param("autoCompleteThreshold") LocalDateTime autoCompleteThreshold,
            @Param("reminderThreshold") LocalDateTime reminderThreshold
    );
    
  
    @Query("SELECT o FROM OrderEntity o WHERE o.status = :status AND o.shippingProvider = :provider")
    List<OrderEntity> findByStatusAndShippingProvider(
            @Param("status") String status,
            @Param("provider") String provider
    );
    

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.shopId = :shopId AND o.status = :status " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countByShopIdAndStatusAndDateRange(
            @Param("shopId") Long shopId,
            @Param("status") String status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    long countByShopId(Long shopId);

    long countByShopIdAndCreatedAtAfter(Long shopId, LocalDateTime start);

    @Query("SELECT COUNT(o) FROM OrderEntity o WHERE o.shopId = :shopId " +
           "AND o.createdAt >= :start AND o.createdAt < :end")
    long countByShopIdAndCreatedAtBetween(
            @Param("shopId") Long shopId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
           "WHERE o.shopId = :shopId AND o.status IN :statuses")
    Long sumTotalAmountByShopIdAndStatusIn(
            @Param("shopId") Long shopId,
            @Param("statuses") List<String> statuses
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
           "WHERE o.shopId = :shopId AND o.status IN :statuses AND o.createdAt >= :start")
    Long sumTotalAmountByShopIdAndStatusInAndCreatedAtAfter(
            @Param("shopId") Long shopId,
            @Param("statuses") List<String> statuses,
            @Param("start") LocalDateTime start
    );

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM OrderEntity o " +
           "WHERE o.shopId = :shopId AND o.status IN :statuses " +
           "AND o.createdAt >= :start AND o.createdAt < :end")
    Long sumTotalAmountByShopIdAndStatusInAndCreatedAtBetween(
            @Param("shopId") Long shopId,
            @Param("statuses") List<String> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(DISTINCT o.userId) FROM OrderEntity o WHERE o.shopId = :shopId")
    long countDistinctCustomersByShopId(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(DISTINCT o.userId) FROM OrderEntity o " +
           "WHERE o.shopId = :shopId AND o.createdAt >= :start")
    long countDistinctCustomersByShopIdAndCreatedAtAfter(
            @Param("shopId") Long shopId,
            @Param("start") LocalDateTime start
    );

    Page<OrderEntity> findByShopIdAndUserId(Long shopId, Long userId, Pageable pageable);

    @Query(value = """
            SELECT o.userId, COUNT(o.id),
                   COALESCE(SUM(CASE WHEN o.status IN :completedStatuses THEN o.totalAmount ELSE 0 END), 0L),
                   MAX(o.createdAt)
            FROM OrderEntity o
            WHERE o.shopId = :shopId
            GROUP BY o.userId
            ORDER BY SUM(CASE WHEN o.status IN :completedStatuses THEN o.totalAmount ELSE 0 END) DESC
            """,
            countQuery = "SELECT COUNT(DISTINCT o.userId) FROM OrderEntity o WHERE o.shopId = :shopId")
    Page<Object[]> aggregateCustomersByShop(
            @Param("shopId") Long shopId,
            @Param("completedStatuses") List<String> completedStatuses,
            Pageable pageable
    );

    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT user_id FROM orders WHERE shop_id = :shopId GROUP BY user_id HAVING COUNT(*) > 1
            ) returning_customers
            """, nativeQuery = true)
    long countReturningCustomersByShopId(@Param("shopId") Long shopId);

    @Query("SELECT o.userId, COUNT(o.id), " +
           "COALESCE(SUM(CASE WHEN o.status IN :completedStatuses THEN o.totalAmount ELSE 0 END), 0L), " +
           "MIN(o.createdAt), MAX(o.createdAt) " +
           "FROM OrderEntity o WHERE o.shopId = :shopId AND o.userId = :userId " +
           "GROUP BY o.userId")
    List<Object[]> aggregateCustomerByShopAndUserId(
            @Param("shopId") Long shopId,
            @Param("userId") Long userId,
            @Param("completedStatuses") List<String> completedStatuses
    );
   
    @Query("SELECT o FROM OrderEntity o WHERE o.shopId = :shopId AND o.status = :status " +
           "AND o.buyerConfirmed = false ORDER BY o.deliveredAt ASC")
    List<OrderEntity> findPendingConfirmationOrders(
            @Param("shopId") Long shopId,
            @Param("status") String status
    );
}
