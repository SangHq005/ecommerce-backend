package com.example.ecommerce.ecommerce_backend.application.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductStatusHistoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductStatusHistoryJpaRepository;

/**
 * Service for tracking product status changes
 */
@Service
public class ProductStatusHistoryService {

    private static final Logger log = LoggerFactory.getLogger(ProductStatusHistoryService.class);

    private final ProductStatusHistoryJpaRepository historyRepo;

    public ProductStatusHistoryService(ProductStatusHistoryJpaRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    /**
     * Record a seller-initiated status change
     */
    @Transactional
    public void recordSellerChange(Long productId, Long shopId, String previousStatus, 
                                    String newStatus, Long sellerId, String reason) {
        record(productId, shopId, previousStatus, newStatus, sellerId, "SELLER", reason);
    }

    /**
     * Record an admin-initiated status change
     */
    @Transactional
    public void recordAdminChange(Long productId, Long shopId, String previousStatus, 
                                   String newStatus, Long adminId, String reason) {
        record(productId, shopId, previousStatus, newStatus, adminId, "ADMIN", reason);
    }

    /**
     * Record a system-initiated status change
     */
    @Transactional
    public void recordSystemChange(Long productId, Long shopId, String previousStatus, 
                                    String newStatus, String reason) {
        record(productId, shopId, previousStatus, newStatus, null, "SYSTEM", reason);
    }

    private void record(Long productId, Long shopId, String previousStatus, 
                        String newStatus, Long changedBy, String changedByType, String reason) {
        ProductStatusHistoryEntity history = new ProductStatusHistoryEntity();
        history.setProductId(productId);
        history.setShopId(shopId);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedByType(changedByType);
        history.setReason(reason);
        
        historyRepo.save(history);
        log.info("Recorded product {} status change: {} -> {} by {} ({})", 
                productId, previousStatus, newStatus, changedByType, reason);
    }

    /**
     * Get status history for a product
     */
    @Transactional(readOnly = true)
    public List<ProductStatusHistoryEntity> getHistoryByProduct(Long productId) {
        return historyRepo.findByProductIdOrderByCreatedAtDesc(productId);
    }

    /**
     * Get status history for a shop
     */
    @Transactional(readOnly = true)
    public Page<ProductStatusHistoryEntity> getHistoryByShop(Long shopId, Pageable pageable) {
        return historyRepo.findByShopIdOrderByCreatedAtDesc(shopId, pageable);
    }
}
