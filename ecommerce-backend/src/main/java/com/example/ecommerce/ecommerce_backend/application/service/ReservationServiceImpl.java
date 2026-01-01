package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductSkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.StockReservationEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductSkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ProductSkuJpaRepository skuRepo;
    private final StockReservationJpaRepository resRepo;

    public ReservationServiceImpl(ProductSkuJpaRepository skuRepo, StockReservationJpaRepository resRepo) {
        this.skuRepo = skuRepo;
        this.resRepo = resRepo;
    }

    @Override
    @Transactional
    public void reserve(String orderToken, Long skuId, Integer qty) {
        // idempotent by (orderToken, skuId)
        var existing = resRepo.findByOrderTokenAndSkuId(orderToken, skuId).orElse(null);
        if (existing != null) {
            if (!"RESERVED".equals(existing.getStatus())) {
                throw ApiException.conflict("Reservation already " + existing.getStatus());
            }
            if (!existing.getQty().equals(qty)) {
                throw ApiException.conflict("Reservation qty mismatch for same order/sku");
            }
            return;
        }

        ProductSkuEntity sku = skuRepo.findByIdForUpdate(skuId)
                .orElseThrow(() -> ApiException.notFound("SKU not found: " + skuId));

        int available = sku.getStockOnHand() - sku.getReservedStock();
        if (available < qty) {
            throw ApiException.conflict("Insufficient stock. available=" + available + ", requested=" + qty);
        }

        sku.setReservedStock(sku.getReservedStock() + qty);
        skuRepo.save(sku);

        StockReservationEntity r = new StockReservationEntity();
        r.setOrderToken(orderToken);
        r.setSkuId(skuId);
        r.setQty(qty);
        r.setStatus("RESERVED");
        resRepo.save(r);
    }

    @Override
    @Transactional
    public void release(String orderToken) {
        var list = resRepo.findByOrderToken(orderToken);
        for (var r : list) {
            if (!"RESERVED".equals(r.getStatus())) continue;

            ProductSkuEntity sku = skuRepo.findByIdForUpdate(r.getSkuId())
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
        // commit reservation => subtract stock_on_hand and reserved_stock, mark COMMITTED
        var list = resRepo.findByOrderToken(orderToken);
        for (var r : list) {
            if (!"RESERVED".equals(r.getStatus())) continue;

            ProductSkuEntity sku = skuRepo.findByIdForUpdate(r.getSkuId())
                    .orElseThrow(() -> ApiException.notFound("SKU not found: " + r.getSkuId()));

            int newOnHand = sku.getStockOnHand() - r.getQty();
            if (newOnHand < 0) throw ApiException.conflict("Stock underflow at commit");

            sku.setReservedStock(Math.max(0, sku.getReservedStock() - r.getQty()));
            sku.setStockOnHand(newOnHand);
            
            skuRepo.save(sku);

            r.setStatus("COMMITTED");
            resRepo.save(r);
        }
    }
}
