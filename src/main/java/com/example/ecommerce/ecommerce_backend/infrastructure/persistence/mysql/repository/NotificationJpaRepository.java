package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationJpaRepository extends JpaRepository<NotificationEntity, Long> {

    Page<NotificationEntity> findByUserId(Long userId, Pageable pageable);

    Page<NotificationEntity> findByUserIdAndIsRead(Long userId, Boolean isRead, Pageable pageable);

    List<NotificationEntity> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead);

    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = cast(CURRENT_TIMESTAMP as Instant) WHERE n.userId = ?1 AND n.isRead = false")
    void markAllAsRead(Long userId);

    @Modifying
    @Query("UPDATE NotificationEntity n SET n.isRead = true, n.readAt = cast(CURRENT_TIMESTAMP as Instant) WHERE n.id = ?1")
    void markAsRead(Long notificationId);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.isRead = true AND n.createdAt < ?1")
    int deleteOldReadNotifications(java.time.LocalDateTime cutoffDate);

    @Modifying
    @Query("DELETE FROM NotificationEntity n WHERE n.isRead = false AND n.createdAt < ?1")
    int deleteOldUnreadNotifications(java.time.LocalDateTime cutoffDate);
}
