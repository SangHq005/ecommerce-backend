package com.example.ecommerce.ecommerce_backend.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ShopStatusHistoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ShopStatusHistoryJpaRepository;

/**
 * Service for managing shop status history.
 * Records all status changes for audit trail and compliance.
 */
@Service
public class ShopStatusHistoryService {
    
    private static final Logger log = LoggerFactory.getLogger(ShopStatusHistoryService.class);
    
    private final ShopStatusHistoryJpaRepository historyRepository;
    
    public ShopStatusHistoryService(ShopStatusHistoryJpaRepository historyRepository) {
        this.historyRepository = historyRepository;
    }
    
    // ===== RECORD STATUS CHANGES =====
    
    /**
     * Record a system-initiated status change
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordSystemChange(Long shopId, String fromStatus, String toStatus, String reason) {
        recordChange(shopId, fromStatus, toStatus, "SYSTEM", null, reason, null);
    }
    
    /**
     * Record a seller-initiated status change
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordSellerChange(Long shopId, String fromStatus, String toStatus, Long sellerId, String reason) {
        recordChange(shopId, fromStatus, toStatus, "SELLER", sellerId, reason, null);
    }
    
    /**
     * Record an admin-initiated status change
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordAdminChange(Long shopId, String fromStatus, String toStatus, Long adminId, String reason) {
        recordChange(shopId, fromStatus, toStatus, "ADMIN", adminId, reason, null);
    }
    
    /**
     * Record a status change with full details
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordChange(Long shopId, String fromStatus, String toStatus, 
                              String actorType, Long actorId, String reason, String note) {
        ShopStatusHistoryEntity history = new ShopStatusHistoryEntity(
            shopId, fromStatus, toStatus, actorType, actorId, reason
        );
        history.setNote(note);
        
        historyRepository.save(history);
        
        log.info("Shop {} status changed: {} -> {} by {} ({}). Reason: {}", 
                shopId, fromStatus, toStatus, actorType, actorId, reason);
    }
    
    // ===== QUERY METHODS =====
    
    /**
     * Get complete history for a shop
     */
    @Transactional(readOnly = true)
    public List<ShopStatusHistoryEntity> getShopHistory(Long shopId) {
        return historyRepository.findByShopIdOrderByCreatedAtDesc(shopId);
    }
    
    /**
     * Get the most recent status change
     */
    @Transactional(readOnly = true)
    public ShopStatusHistoryEntity getLatestChange(Long shopId) {
        return historyRepository.findFirstByShopIdOrderByCreatedAtDesc(shopId).orElse(null);
    }
    
    /**
     * Get history entries for a specific status
     */
    @Transactional(readOnly = true)
    public List<ShopStatusHistoryEntity> getHistoryByStatus(Long shopId, String status) {
        return historyRepository.findByShopIdAndToStatusOrderByCreatedAtDesc(shopId, status);
    }
    
    /**
     * Get all changes made by a specific actor
     */
    @Transactional(readOnly = true)
    public List<ShopStatusHistoryEntity> getChangesByActor(String actorType, Long actorId) {
        return historyRepository.findByActor(actorType, actorId);
    }
    
    /**
     * Count total status changes for a shop
     */
    @Transactional(readOnly = true)
    public long countChanges(Long shopId) {
        return historyRepository.countByShopId(shopId);
    }
}
