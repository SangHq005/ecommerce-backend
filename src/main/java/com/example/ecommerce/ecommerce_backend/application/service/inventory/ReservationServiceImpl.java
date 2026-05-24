package com.example.ecommerce.ecommerce_backend.application.service.inventory;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.notification.NotificationService;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.InventoryLogEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.InventoryLogJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final SkuJpaRepository skuRepo;
    private final StockReservationJpaRepository resRepo;
    private final ProductJpaRepository productRepo;
    private final SellerShopJpaRepository shopRepo;
    private final NotificationService notificationService;
    private final InventoryLogJpaRepository inventoryLogRepo;

    public ReservationServiceImpl(
            SkuJpaRepository skuRepo,
            StockReservationJpaRepository resRepo,
            ProductJpaRepository productRepo,
            SellerShopJpaRepository shopRepo,
            NotificationService notificationService,
            InventoryLogJpaRepository inventoryLogRepo
    ) {
        this.skuRepo = skuRepo;
        this.resRepo = resRepo;
        this.productRepo = productRepo;
        this.shopRepo = shopRepo;
        this.notificationService = notificationService;
        this.inventoryLogRepo = inventoryLogRepo;
    }

    @Override
    @Transactional
    public void reserve(String orderToken, Long skuId, Integer qty) {
        // Idempotent check by (orderToken, skuId)
        var existing = resRepo.findByOrderTokenAndSkuId(orderToken, skuId).orElse(null);
        if (existing != null) {
            if (!"RESERVED".equals(existing.getStatus())) {
                throw ApiException.conflict("Reservation already " + existing.getStatus());
            }
            if (!existing.getQty().equals(qty)) {
                throw ApiException.conflict("Reservation qty mismatch for same order/sku");
            }
            return; // Already reserved with same qty - idempotent success
        }

        // Pessimistic lock on SKU row to prevent concurrent stock modifications
        SkuEntity sku = skuRepo.findByIdForUpdate(skuId)
                .orElseThrow(() -> ApiException.notFound("SKU not found: " + skuId));

        // Calculate available stock (onHand - reserved)
        int available = sku.getStockOnHand() - sku.getReservedStock();
        if (available < qty) {
            System.err.println("RESERVATION FAIL: sku=" + skuId + ", onHand=" + sku.getStockOnHand() + ", reserved=" + sku.getReservedStock() + ", req=" + qty + ", token=" + orderToken);
            // Use specific error code for frontend to display user-friendly message
            throw ApiException.insufficientStock("Sản phẩm không đủ số lượng. Còn lại: " + available + ", yêu cầu: " + qty);
        }

        // Low Stock Alert: Notify seller when crossing threshold (5 items)
        if (available > 5 && (available - qty) <= 5) {
            try {
                var product = productRepo.findById(sku.getProductId()).orElse(null);
                if (product != null) {
                    var shop = shopRepo.findById(product.getShopId()).orElse(null);
                    if (shop != null) {
                        notificationService.createNotification(
                                shop.getSellerUserId(),
                                "INVENTORY_LOW",
                                "Cảnh báo sắp hết hàng",
                                "Sản phẩm " + product.getName() + " (SKU: " + sku.getSkuCode() + ") chỉ còn lại " + (available - qty) + " sản phẩm.",
                                "PRODUCT",
                                product.getId()
                        );
                    }
                }
            } catch (Exception e) {
                // Non-blocking - notification failure should not affect checkout
                System.err.println("Failed to send low stock notification: " + e.getMessage());
            }
        }

        // Reserve the stock
        sku.setReservedStock(sku.getReservedStock() + qty);
        skuRepo.save(sku);

        // Create reservation record
        StockReservationEntity r = new StockReservationEntity();
        r.setOrderToken(orderToken);
        r.setSkuId(skuId);
        r.setQty(qty);
        r.setStatus("RESERVED");
        r.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Reservation expires in 15 minutes
        resRepo.save(r);
    }

    @Override
    @Transactional
    public void release(String orderToken) {
        var list = resRepo.findByOrderToken(orderToken);
        for (var r : list) {
            if (!"RESERVED".equals(r.getStatus())) continue;

            SkuEntity sku = skuRepo.findByIdForUpdate(r.getSkuId())
                    .orElseThrow(() -> ApiException.notFound("SKU not found: " + r.getSkuId()));

            sku.setReservedStock(Math.max(0, sku.getReservedStock() - r.getQty()));
            skuRepo.save(sku);

            r.setStatus("RELEASED");
            resRepo.save(r);
        }
    }

    @Override
    @Transactional
    public void commit(String orderToken) {
        // Commit reservation => subtract stock_on_hand and reserved_stock, mark COMMITTED
        var list = resRepo.findByOrderToken(orderToken);
        for (var r : list) {
            if (!"RESERVED".equals(r.getStatus())) continue;

            SkuEntity sku = skuRepo.findByIdForUpdate(r.getSkuId())
                    .orElseThrow(() -> ApiException.notFound("SKU not found: " + r.getSkuId()));

            int previousStock = sku.getStockOnHand();
            int newOnHand = sku.getStockOnHand() - r.getQty();
            if (newOnHand < 0) {
                throw ApiException.insufficientStock("Stock underflow at commit. Cannot fulfill order.");
            }

            sku.setReservedStock(Math.max(0, sku.getReservedStock() - r.getQty()));
            sku.setStockOnHand(newOnHand);
            
            skuRepo.save(sku);

            // Sync total stock to ProductEntity for search/display
            productRepo.findById(sku.getProductId()).ifPresent(p -> {
                p.setStockQuantity(Math.max(0, p.getStockQuantity() - r.getQty()));
                productRepo.save(p);
                
                // Inventory Log
                InventoryLogEntity log = new InventoryLogEntity();
                log.setShopId(p.getShopId());
                log.setProductId(sku.getProductId());
                log.setSkuId(sku.getId());
                log.setChangeAmount(-r.getQty());
                log.setPreviousStock(previousStock);
                log.setNewStock(newOnHand);
                log.setReason("ORDER_PLACED");
                log.setReferenceId(orderToken); // Using OrderToken which is OrderCode
                log.setActorId(null); // System
                inventoryLogRepo.save(log);
            });

            r.setStatus("COMMITTED");
            resRepo.save(r);
        }
    }

    public void restore(String orderToken) {
        var list = resRepo.findByOrderToken(orderToken);
        for (var r : list) {
             // Only restore if it was COMMITTED (stock deducted).
             // If it was RESERVED, call release() instead or handle it here?
             // Assuming release() handles RESERVED, this handles COMMITTED.
             if (!"COMMITTED".equals(r.getStatus())) continue;

             SkuEntity sku = skuRepo.findByIdForUpdate(r.getSkuId())
                     .orElseThrow(() -> ApiException.notFound("SKU not found: " + r.getSkuId()));
             
             int previousStock = sku.getStockOnHand();
             int newOnHand = previousStock + r.getQty();
             
             sku.setStockOnHand(newOnHand);
             skuRepo.save(sku);
             
             productRepo.findById(sku.getProductId()).ifPresent(p -> {
                 p.setStockQuantity(p.getStockQuantity() + r.getQty());
                 productRepo.save(p);
                 
                 InventoryLogEntity log = new InventoryLogEntity();
                 log.setShopId(p.getShopId());
                 log.setProductId(sku.getProductId());
                 log.setSkuId(sku.getId());
                 log.setChangeAmount(r.getQty());
                 log.setPreviousStock(previousStock);
                 log.setNewStock(newOnHand);
                 log.setReason("ORDER_CANCELLED"); // or RESTOCK
                 log.setReferenceId(orderToken); 
                 inventoryLogRepo.save(log);
             });
             
             r.setStatus("RESTORED");
             resRepo.save(r);
        }
    }
}
