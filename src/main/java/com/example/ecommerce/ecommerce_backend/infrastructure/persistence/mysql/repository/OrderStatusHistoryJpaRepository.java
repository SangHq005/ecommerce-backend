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

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderStatusHistoryEntity.ActorType;

@Repository
public interface OrderStatusHistoryJpaRepository extends JpaRepository<OrderStatusHistoryEntity, Long> {

    // Find all history for an order (ordered by time)
    List<OrderStatusHistoryEntity> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    // Find all history for an order (descending for latest first)
    List<OrderStatusHistoryEntity> findByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Find paginated history
    Page<OrderStatusHistoryEntity> findByOrderId(Long orderId, Pageable pageable);

    // Find latest status change for an order
    Optional<OrderStatusHistoryEntity> findFirstByOrderIdOrderByCreatedAtDesc(Long orderId);

    // Find history by actor
    List<OrderStatusHistoryEntity> findByOrderIdAndActorType(Long orderId, ActorType actorType);

    // Find history by status transition
    @Query("SELECT h FROM OrderStatusHistoryEntity h WHERE h.orderId = :orderId AND h.toStatus = :status")
    List<OrderStatusHistoryEntity> findByOrderIdAndToStatus(@Param("orderId") Long orderId, @Param("status") String status);

    // Find when order reached a specific status
    @Query("SELECT h FROM OrderStatusHistoryEntity h WHERE h.orderId = :orderId AND h.toStatus = :status ORDER BY h.createdAt ASC")
    Optional<OrderStatusHistoryEntity> findFirstTimeReachedStatus(@Param("orderId") Long orderId, @Param("status") String status);

    // Find history in time range
    @Query("SELECT h FROM OrderStatusHistoryEntity h WHERE h.orderId = :orderId " +
           "AND h.createdAt BETWEEN :start AND :end ORDER BY h.createdAt ASC")
    List<OrderStatusHistoryEntity> findByOrderIdAndTimeRange(
            @Param("orderId") Long orderId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    // Count status changes by actor type
    long countByOrderIdAndActorType(Long orderId, ActorType actorType);

    // Find all history for multiple orders
    @Query("SELECT h FROM OrderStatusHistoryEntity h WHERE h.orderId IN :orderIds ORDER BY h.orderId, h.createdAt ASC")
    List<OrderStatusHistoryEntity> findByOrderIds(@Param("orderIds") List<Long> orderIds);

    // Find recent activity by actor
    @Query("SELECT h FROM OrderStatusHistoryEntity h WHERE h.actorType = :actorType AND h.actorId = :actorId " +
           "ORDER BY h.createdAt DESC")
    Page<OrderStatusHistoryEntity> findRecentByActor(
            @Param("actorType") ActorType actorType,
            @Param("actorId") Long actorId,
            Pageable pageable);
}
