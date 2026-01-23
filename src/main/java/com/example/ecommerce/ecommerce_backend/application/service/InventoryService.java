package com.example.ecommerce.ecommerce_backend.application.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.InventoryLogEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.InventoryLogJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final SkuJpaRepository skuRepo;
    private final InventoryLogJpaRepository logRepo;
    private final ProductJpaRepository productRepo;

    public InventoryService(SkuJpaRepository skuRepo, InventoryLogJpaRepository logRepo, ProductJpaRepository productRepo) {
        this.skuRepo = skuRepo;
        this.logRepo = logRepo;
        this.productRepo = productRepo;
    }
    
    // === DTOs ===
    
    public record BatchAdjustmentRequest(Long skuId, int delta, String reason) {}
    
    public record BatchAdjustmentResult(Long skuId, boolean success, String message, Integer newStock) {}
    
    public record LowStockAlert(Long productId, String productName, Long skuId, String skuCode, 
                                 int currentStock, int threshold, String severity) {}
    
    public record InventorySummary(
            long totalSkus,
            long lowStockSkus,
            long outOfStockSkus,
            long healthyStockSkus,
            long totalStockValue,
            List<LowStockAlert> lowStockAlerts
    ) {}

    @Transactional
    public SkuEntity adjustStock(Long skuId, int delta, String reason, String referenceId, Long actorId) {
        SkuEntity sku = skuRepo.findById(skuId)
                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + skuId));

        int previousStock = sku.getStockOnHand();
        int newStock = previousStock + delta;

        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock. Current: " + previousStock + ", Requested Delta: " + delta);
        }

        sku.setStockOnHand(newStock);
        SkuEntity savedSku = skuRepo.save(sku);

        // Fetch Product to get Shop ID
        var product = productRepo.findById(sku.getProductId()).orElseThrow();

        // Create Log
        InventoryLogEntity log = new InventoryLogEntity();
        log.setShopId(product.getShopId());
        log.setProductId(sku.getProductId());
        log.setSkuId(skuId);
        log.setChangeAmount(delta);
        log.setPreviousStock(previousStock);
        log.setNewStock(newStock);
        log.setReason(reason);
        log.setReferenceId(referenceId);
        log.setActorId(actorId);
        logRepo.save(log);

        return savedSku;
    }

    public Page<InventoryLogEntity> getHistory(Long skuId, Pageable pageable) {
        return logRepo.findBySkuId(skuId, pageable);
    }
    
    public Page<InventoryLogEntity> getShopHistory(Long shopId, Pageable pageable) {
        return logRepo.findByShopId(shopId, pageable);
    }
    
    // ==================== BATCH OPERATIONS (NEW) ====================
    
    /**
     * Batch stock adjustment - atomically adjust multiple SKUs
     */
    @Transactional
    public List<BatchAdjustmentResult> batchAdjustStock(Long shopId, List<BatchAdjustmentRequest> requests, Long actorId) {
        log.info("Processing batch adjustment of {} items for shop {}", requests.size(), shopId);
        
        List<BatchAdjustmentResult> results = new ArrayList<>();
        
        for (BatchAdjustmentRequest req : requests) {
            try {
                SkuEntity updated = adjustStock(req.skuId(), req.delta(), req.reason(), "BATCH_ADJUST", actorId);
                results.add(new BatchAdjustmentResult(req.skuId(), true, "Success", updated.getStockOnHand()));
            } catch (Exception e) {
                log.warn("Failed to adjust SKU {}: {}", req.skuId(), e.getMessage());
                results.add(new BatchAdjustmentResult(req.skuId(), false, e.getMessage(), null));
            }
        }
        
        return results;
    }
    
    // ==================== LOW STOCK ALERTS (NEW) ====================
    
    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 10;
    private static final int CRITICAL_STOCK_THRESHOLD = 5;
    
    /**
     * Get low stock alerts for a shop
     */
    @Transactional
    public List<LowStockAlert> getLowStockAlerts(Long shopId, int threshold) {
        int effectiveThreshold = threshold > 0 ? threshold : DEFAULT_LOW_STOCK_THRESHOLD;
        
        List<ProductEntity> products = productRepo.findByShopIdAndStatus(shopId, "ACTIVE");
        List<LowStockAlert> alerts = new ArrayList<>();
        
        for (ProductEntity product : products) {
            List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(product.getId());
            
            for (SkuEntity sku : skus) {
                if (sku.isActive() && sku.getStockOnHand() <= effectiveThreshold) {
                    String severity = sku.getStockOnHand() == 0 ? "CRITICAL" :
                                      sku.getStockOnHand() <= CRITICAL_STOCK_THRESHOLD ? "HIGH" : "MEDIUM";
                    
                    alerts.add(new LowStockAlert(
                            product.getId(),
                            product.getName(),
                            sku.getId(),
                            sku.getSkuCode(),
                            sku.getStockOnHand(),
                            effectiveThreshold,
                            severity
                    ));
                }
            }
        }
        
        // Sort by severity (CRITICAL first)
        alerts.sort((a, b) -> {
            int severityOrder = getSeverityOrder(a.severity()) - getSeverityOrder(b.severity());
            if (severityOrder != 0) return severityOrder;
            return Integer.compare(a.currentStock(), b.currentStock());
        });
        
        return alerts;
    }
    
    private int getSeverityOrder(String severity) {
        return switch (severity) {
            case "CRITICAL" -> 0;
            case "HIGH" -> 1;
            case "MEDIUM" -> 2;
            default -> 3;
        };
    }
    
    // ==================== INVENTORY SUMMARY (NEW) ====================
    
    /**
     * Get inventory summary for a shop dashboard
     */
    @Transactional
    public InventorySummary getInventorySummary(Long shopId) {
        List<ProductEntity> products = productRepo.findByShopIdAndStatus(shopId, "ACTIVE");
        
        long totalSkus = 0;
        long lowStockSkus = 0;
        long outOfStockSkus = 0;
        long healthyStockSkus = 0;
        long totalStockValue = 0;
        
        List<LowStockAlert> topAlerts = new ArrayList<>();
        
        for (ProductEntity product : products) {
            List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(product.getId());
            
            for (SkuEntity sku : skus) {
                if (!sku.isActive()) continue;
                
                totalSkus++;
                int stock = sku.getStockOnHand();
                totalStockValue += stock * sku.getPrice();
                
                if (stock == 0) {
                    outOfStockSkus++;
                    topAlerts.add(new LowStockAlert(product.getId(), product.getName(), 
                            sku.getId(), sku.getSkuCode(), stock, DEFAULT_LOW_STOCK_THRESHOLD, "CRITICAL"));
                } else if (stock <= CRITICAL_STOCK_THRESHOLD) {
                    lowStockSkus++;
                    topAlerts.add(new LowStockAlert(product.getId(), product.getName(), 
                            sku.getId(), sku.getSkuCode(), stock, DEFAULT_LOW_STOCK_THRESHOLD, "HIGH"));
                } else if (stock <= DEFAULT_LOW_STOCK_THRESHOLD) {
                    lowStockSkus++;
                    topAlerts.add(new LowStockAlert(product.getId(), product.getName(), 
                            sku.getId(), sku.getSkuCode(), stock, DEFAULT_LOW_STOCK_THRESHOLD, "MEDIUM"));
                } else {
                    healthyStockSkus++;
                }
            }
        }
        
        // Only keep top 10 alerts
        List<LowStockAlert> limitedAlerts = topAlerts.stream()
                .sorted((a, b) -> getSeverityOrder(a.severity()) - getSeverityOrder(b.severity()))
                .limit(10)
                .toList();
        
        return new InventorySummary(totalSkus, lowStockSkus, outOfStockSkus, 
                                     healthyStockSkus, totalStockValue, limitedAlerts);
    }
    
    // ==================== STOCK RESERVATION (NEW) ====================
    
    /**
     * Reserve stock for checkout (prevents overselling)
     */
    @Transactional
    public void reserveStock(Long skuId, int quantity, String referenceId) {
        SkuEntity sku = skuRepo.findById(skuId)
                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + skuId));
        
        int available = sku.getStockOnHand() - sku.getReservedStock();
        if (available < quantity) {
            throw new IllegalArgumentException("Insufficient available stock. Available: " + available);
        }
        
        sku.setReservedStock(sku.getReservedStock() + quantity);
        skuRepo.save(sku);
        
        log.info("Reserved {} units of SKU {} for {}", quantity, skuId, referenceId);
    }
    
    /**
     * Release reserved stock (e.g., when order is cancelled or expires)
     */
    @Transactional
    public void releaseReservedStock(Long skuId, int quantity, String referenceId) {
        SkuEntity sku = skuRepo.findById(skuId)
                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + skuId));
        
        int newReserved = Math.max(0, sku.getReservedStock() - quantity);
        sku.setReservedStock(newReserved);
        skuRepo.save(sku);
        
        log.info("Released {} reserved units of SKU {} for {}", quantity, skuId, referenceId);
    }
    
    /**
     * Commit reserved stock (convert reservation to actual sale)
     */
    @Transactional
    public void commitReservedStock(Long skuId, int quantity, String referenceId, Long actorId) {
        SkuEntity sku = skuRepo.findById(skuId)
                .orElseThrow(() -> new EntityNotFoundException("SKU not found: " + skuId));
        
        if (sku.getReservedStock() < quantity) {
            throw new IllegalArgumentException("Insufficient reserved stock");
        }
        
        // Release reservation
        sku.setReservedStock(sku.getReservedStock() - quantity);
        
        // Deduct from actual stock
        int previousStock = sku.getStockOnHand();
        int newStock = previousStock - quantity;
        
        if (newStock < 0) {
            throw new IllegalArgumentException("Insufficient stock to commit");
        }
        
        sku.setStockOnHand(newStock);
        skuRepo.save(sku);
        
        // Log the change
        var product = productRepo.findById(sku.getProductId()).orElseThrow();
        
        InventoryLogEntity logEntry = new InventoryLogEntity();
        logEntry.setShopId(product.getShopId());
        logEntry.setProductId(sku.getProductId());
        logEntry.setSkuId(skuId);
        logEntry.setChangeAmount(-quantity);
        logEntry.setPreviousStock(previousStock);
        logEntry.setNewStock(newStock);
        logEntry.setReason("ORDER_COMMITTED");
        logEntry.setReferenceId(referenceId);
        logEntry.setActorId(actorId);
        logRepo.save(logEntry);
        
        log.info("Committed {} units of SKU {} for order {}", quantity, skuId, referenceId);
    }
    
    // ==================== REPORTS (NEW) ====================
    
    /**
     * Get stock movement report for a period
     */
    public Map<String, Long> getStockMovementSummary(Long shopId, int daysBack) {
        Instant since = Instant.now().minus(daysBack, ChronoUnit.DAYS);
        List<InventoryLogEntity> logs = logRepo.findByShopIdAndCreatedAtAfter(shopId, since);
        
        long totalIn = 0;
        long totalOut = 0;
        
        for (InventoryLogEntity logEntry : logs) {
            if (logEntry.getChangeAmount() > 0) {
                totalIn += logEntry.getChangeAmount();
            } else {
                totalOut += Math.abs(logEntry.getChangeAmount());
            }
        }
        
        return Map.of(
                "totalIn", totalIn,
                "totalOut", totalOut,
                "netChange", totalIn - totalOut,
                "transactionCount", (long) logs.size()
        );
    }
}
