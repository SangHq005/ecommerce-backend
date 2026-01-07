package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductSkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductSkuJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository itemRepo;
    private final ProductSkuJpaRepository skuRepo;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository productRepo;
    private final ReservationService reservationService;
    private final IdempotencyService idempotencyService;
    private final ObjectMapper om;

    public OrderService(
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository itemRepo,
            ProductSkuJpaRepository skuRepo,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository productRepo,
            ReservationService reservationService,
            IdempotencyService idempotencyService,
            ObjectMapper om
    ) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.skuRepo = skuRepo;
        this.productRepo = productRepo;
        this.reservationService = reservationService;
        this.idempotencyService = idempotencyService;
        this.om = om;
    }

    @Transactional
    public List<OrderResponse> checkout(Long userId, String idemKey, CheckoutRequest req) {
        if (idemKey == null || idemKey.isBlank()) throw ApiException.badRequest("Missing Idempotency-Key");

        String requestHash = RequestHash.sha256(req.toString());

        var replay = idempotencyService.begin("order.checkout", idemKey, requestHash, Duration.ofMinutes(10));
        if (replay != null) {
            try {
                // Deserialize list
                return om.readValue(replay.responseBodyJson(), om.getTypeFactory().constructCollectionType(List.class, OrderResponse.class));
            } catch (Exception e) {
                // fallback or throw?
                throw ApiException.conflict("Stored idempotent response cannot be parsed");
            }
        }

        // Prepare grouping
        // Tuple: Item, Sku, Product
        record ItemContext(CheckoutRequest.Item item, ProductSkuEntity sku, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity product) {}

        java.util.Map<Long, List<ItemContext>> byShop = new java.util.HashMap<>();

        for (var it : req.items()) {
            ProductSkuEntity sku = skuRepo.findById(it.skuId())
                    .orElseThrow(() -> ApiException.notFound("SKU not found: " + it.skuId()));
            
            if (!sku.getProductId().equals(it.productId())) {
                throw ApiException.badRequest("skuId " + it.skuId() + " does not belong to productId " + it.productId());
            }

            var product = productRepo.findById(sku.getProductId())
                    .orElseThrow(() -> ApiException.notFound("Product not found: " + sku.getProductId()));

            byShop.computeIfAbsent(product.getShopId(), k -> new ArrayList<>())
                    .add(new ItemContext(it, sku, product));
        }

        List<OrderResponse> responses = new ArrayList<>();
        long now = System.currentTimeMillis();
        int idx = 0;

        for (var entry : byShop.entrySet()) {
            Long shopId = entry.getKey();
            List<ItemContext> shopItems = entry.getValue();
            idx++;
            
            // Format: OD{timestamp}-{userId}-{shopId}-{index} to be safe/unique
            String orderCode = "OD" + now + "_" + userId + "_" + shopId + "_" + idx;

            OrderEntity o = new OrderEntity();
            o.setOrderCode(orderCode);
            o.setUserId(userId);
            o.setShopId(shopId);
            o.setStatus(OrderStatus.SUBMITTED.name());
            o.setCurrency("VND");
            o.setTotalAmount(0L);
            orderRepo.save(o);

            long total = 0;
            List<OrderResponse.Item> respItems = new ArrayList<>();

            for (var ctx : shopItems) {
                // Reserve stock
                reservationService.reserve(orderCode, ctx.sku.getId(), ctx.item.quantity());

                long unitPrice = ctx.sku.getPrice();
                long lineTotal = unitPrice * ctx.item.quantity();
                total += lineTotal;

                OrderItemEntity oi = new OrderItemEntity();
                oi.setOrderId(o.getId());
                oi.setProductId(ctx.product.getId());
                oi.setSkuId(ctx.sku.getId());
                oi.setQuantity(ctx.item.quantity());
                oi.setUnitPrice(unitPrice);
                oi.setTotalPrice(lineTotal);
                itemRepo.save(oi);

                respItems.add(new OrderResponse.Item(ctx.product.getId(), ctx.sku.getId(), ctx.item.quantity(), unitPrice, lineTotal));
            }

            o.setTotalAmount(total);
            o.setStatus(OrderStatus.PAYMENT_PENDING.name());
            orderRepo.save(o);

            responses.add(new OrderResponse(orderCode, o.getStatus(), total, o.getCurrency(), respItems));
        }

        try {
            idempotencyService.complete("order.checkout", idemKey, 200, om.writeValueAsString(responses));
        } catch (Exception e) {
            // log error
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long userId, String orderCode) {
        OrderEntity o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");

        var items = itemRepo.findByOrderId(o.getId());
        List<OrderResponse.Item> respItems = new ArrayList<>();
        for (var i : items) {
            respItems.add(new OrderResponse.Item(i.getProductId(), i.getSkuId(), i.getQuantity(), i.getUnitPrice(), i.getTotalPrice()));
        }
        return new OrderResponse(o.getOrderCode(), o.getStatus(), o.getTotalAmount(), o.getCurrency(), respItems);
    }

    @Transactional
    public void cancel(Long userId, String orderCode) {
        OrderEntity o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");

        if (!OrderStatus.PAYMENT_PENDING.name().equals(o.getStatus())) {
            throw ApiException.conflict("Only PAYMENT_PENDING can be cancelled");
        }

        o.setStatus(OrderStatus.CANCELLED.name());
        orderRepo.save(o);

        reservationService.release(orderCode);
    }
}
