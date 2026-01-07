package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.EventLogDocument;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AdminShopService {

    private final SellerShopJpaRepository shopRepo;
    private final StringRedisTemplate redis;
    private final EventLogMongoRepository eventRepo;

    public AdminShopService(SellerShopJpaRepository shopRepo, StringRedisTemplate redis, EventLogMongoRepository eventRepo) {
        this.shopRepo = shopRepo;
        this.redis = redis;
        this.eventRepo = eventRepo;
    }

    public List<SellerShopEntity> listByStatus(String status) {
        return shopRepo.findByStatus(status);
    }

    @Transactional
    public SellerShopEntity approve(Long shopId) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        if (!"PENDING_REVIEW".equals(s.getStatus())) throw new IllegalArgumentException("Only PENDING_REVIEW can approve");
        s.setStatus("ACTIVE");
        s.setVerifiedAt(Instant.now());
        s.setSuspendedReason(null);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "ACTIVE")));
        return saved;
    }

    @Transactional
    public SellerShopEntity suspend(Long shopId, String reason) {
        SellerShopEntity s = shopRepo.findById(shopId).orElseThrow();
        if ("SUSPENDED".equals(s.getStatus())) return s;
        s.setStatus("SUSPENDED");
        s.setSuspendedReason(reason);
        SellerShopEntity saved = shopRepo.save(s);
        invalidate(saved);

        eventRepo.save(new EventLogDocument("SHOP_STATUS_CHANGED", "shop_" + saved.getId(), Instant.now(), null,
                Map.of("shopId", saved.getId(), "status", "SUSPENDED", "reason", reason)));
        return saved;
    }

    private void invalidate(SellerShopEntity s) {
        redis.delete("cache:shop:" + s.getId());
        redis.delete("cache:shop_by_seller:" + s.getSellerUserId());
    }
}
