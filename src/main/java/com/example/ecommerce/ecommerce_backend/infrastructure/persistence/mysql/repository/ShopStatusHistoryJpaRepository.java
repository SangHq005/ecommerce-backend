package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ShopStatusHistoryEntity;

@Repository
public interface ShopStatusHistoryJpaRepository extends JpaRepository<ShopStatusHistoryEntity, Long> {
    
    /**
     * Find all history entries for a shop, ordered by most recent first
     */
    List<ShopStatusHistoryEntity> findByShopIdOrderByCreatedAtDesc(Long shopId);
    
    /**
     * Find the most recent status change for a shop
     */
    Optional<ShopStatusHistoryEntity> findFirstByShopIdOrderByCreatedAtDesc(Long shopId);
    
    /**
     * Find history entries by status
     */
    List<ShopStatusHistoryEntity> findByShopIdAndToStatusOrderByCreatedAtDesc(Long shopId, String toStatus);
    
    /**
     * Count status changes for a shop
     */
    long countByShopId(Long shopId);
    
    /**
     * Find all history entries by actor
     */
    @Query("SELECT h FROM ShopStatusHistoryEntity h WHERE h.actorType = :actorType AND h.actorId = :actorId ORDER BY h.createdAt DESC")
    List<ShopStatusHistoryEntity> findByActor(@Param("actorType") String actorType, @Param("actorId") Long actorId);
}
