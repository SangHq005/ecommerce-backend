package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminShopService {

    private final SellerShopJpaRepository shopRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;
    private final ShopStatusHistoryService historyService;

    public AdminShopService(SellerShopJpaRepository shopRepo, StringRedisTemplate redis, 
                            EventLogMongoRepository eventRepo, ShopStatusHistoryService historyService) {
        this.shopRepo = shopRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
        this.historyService = historyService;
    }

    public List<SellerShopEntity> listByStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return shopRepo.findAll();
        }
        return shopRepo.findByStatus(status);
    }

    @Transactional
    public SellerShopEntity approve(Long shopId, Long adminId) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        if (!"PENDING_REVIEW".equals(s.getStatus())) throw new IllegalArgumentException("Only PENDING_REVIEW can approve");
        
        String previousStatus = s.getStatus();
        s.setStatus("ACTIVE");
        s.setVerifiedAt(Instant.now());
        s.setSuspendedReason(null);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "ACTIVE", "adminId", adminId)));
        
        // Record status history
        historyService.recordAdminChange(saved.getId(), previousStatus, "ACTIVE", adminId, "Shop approved");
        
        return saved;
    }

    @Transactional
    public SellerShopEntity suspend(Long shopId, String reason, Long adminId) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        if ("SUSPENDED".equals(s.getStatus())) return s;
        
        String previousStatus = s.getStatus();
        s.setStatus("SUSPENDED");
        s.setSuspendedReason(reason);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "SUSPENDED", "reason", reason, "adminId", adminId)));
        
        // Record status history
        historyService.recordAdminChange(saved.getId(), previousStatus, "SUSPENDED", adminId, reason);
        
        return saved;
    }

    @Transactional
    public SellerShopEntity reject(Long shopId, String reason, Long adminId) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        // Allow rejecting if not already active or if explicitly needed. Usually reject is for PENDING_REVIEW.
        if ("ACTIVE".equals(s.getStatus())) throw new IllegalArgumentException("Cannot reject already ACTIVE shop. Suspend instead.");
        
        String previousStatus = s.getStatus();
        s.setStatus("REJECTED");
        s.setSuspendedReason(reason);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "REJECTED", "reason", reason, "adminId", adminId)));
        
        // Record status history
        historyService.recordAdminChange(saved.getId(), previousStatus, "REJECTED", adminId, reason);
        
        return saved;
    }
    
    /**
     * Reactivate a suspended shop
     */
    @Transactional
    public SellerShopEntity reactivate(Long shopId, Long adminId) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        if (!"SUSPENDED".equals(s.getStatus())) {
            throw new IllegalArgumentException("Only SUSPENDED shops can be reactivated");
        }
        
        String previousStatus = s.getStatus();
        s.setStatus("ACTIVE");
        s.setSuspendedReason(null);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "ACTIVE", "adminId", adminId, "action", "REACTIVATED")));
        
        // Record status history
        historyService.recordAdminChange(saved.getId(), previousStatus, "ACTIVE", adminId, "Shop reactivated by admin");
        
        return saved;
    }

    private void invalidate(SellerShopEntity s) {
        redis.delete("cache:shop:" + s.getId());
        redis.delete("cache:shop_by_seller:" + s.getSellerUserId());
    }
}
