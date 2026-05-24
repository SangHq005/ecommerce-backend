package com.example.ecommerce.ecommerce_backend.application.service.order;

import com.example.ecommerce.ecommerce_backend.application.service.common.RequestHash;

import java.time.Duration;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import java.util.ArrayList;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import java.util.List;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import java.util.stream.Collectors;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import org.springframework.stereotype.Service;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import org.springframework.transaction.annotation.Transactional;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository;
import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;

import com.example.ecommerce.ecommerce_backend.application.service.inventory.ReservationService;
import com.example.ecommerce.ecommerce_backend.application.service.common.IdempotencyService;
import com.example.ecommerce.ecommerce_backend.application.service.coupon.CouponService;
@Service
public class OrderService {

    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository itemRepo;
    private final SkuJpaRepository skuRepo;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository productRepo;
    private final UserAddressJpaRepository addressRepo;
    private final ReservationService reservationService;
    private final IdempotencyService idempotencyService;
    private final CouponService couponService;
    private final ObjectMapper om;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.config.OrderProperties orderProperties;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper.OrderDomainMapper orderMapper;
    private final OrderStatusHistoryService orderHistoryService;

    public OrderService(
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository itemRepo,
            SkuJpaRepository skuRepo,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository productRepo,
            UserAddressJpaRepository addressRepo,
            ReservationService reservationService,
            IdempotencyService idempotencyService,
            CouponService couponService,
            ObjectMapper om,
            com.example.ecommerce.ecommerce_backend.infrastructure.config.OrderProperties orderProperties,
            com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.mapper.OrderDomainMapper orderMapper,
            OrderStatusHistoryService orderHistoryService
    ) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.skuRepo = skuRepo;
        this.productRepo = productRepo;
        this.addressRepo = addressRepo;
        this.reservationService = reservationService;
        this.idempotencyService = idempotencyService;
        this.couponService = couponService;
        this.om = om;
        this.orderProperties = orderProperties;
        this.orderMapper = orderMapper;
        this.orderHistoryService = orderHistoryService;
    }

    @Transactional
    public List<OrderResponse> checkout(Long userId, String idemKey, CheckoutRequest req) {
        try {
            if (idemKey == null || idemKey.isBlank()) throw ApiException.badRequest("Missing Idempotency-Key");

            // Validate address
            if (!addressRepo.existsByIdAndUserId(req.addressId(), userId)) {
                throw ApiException.notFound("Address not found or does not belong to user");
            }

            String requestHash = RequestHash.sha256(req.toString());

            var replay = idempotencyService.begin("order.checkout", idemKey, requestHash, Duration.ofMinutes(10));
            if (replay != null) {
                String json = replay.responseBodyJson();
                if (json == null || json.isBlank()) {
                    throw ApiException.conflict("Idempotency key locked but no response found");
                }
                try {
                    return om.readValue(json, om.getTypeFactory().constructCollectionType(List.class, OrderResponse.class));
                } catch (Exception e) {
                    System.err.println("IDEMPOTENCY DESERIALIZATION ERROR: " + e.getMessage());
                    throw ApiException.conflict("Stored idempotent response cannot be parsed: " + e.getMessage());
                }
            }

            // --- OPTIMIZATION START: Batch Fetching ---
            List<Long> skuIds = req.items().stream().map(CheckoutRequest.Item::skuId).distinct().toList();
            if (skuIds.isEmpty()) throw ApiException.badRequest("No items in order");

            List<SkuEntity> skus = skuRepo.findAllById(skuIds);
            if (skus.size() != skuIds.size()) {
                throw ApiException.badRequest("One or more SKUs not found");
            }
            java.util.Map<Long, SkuEntity> skuMap = skus.stream()
                    .collect(java.util.stream.Collectors.toMap(SkuEntity::getId, java.util.function.Function.identity()));

            List<Long> productIds = skus.stream().map(SkuEntity::getProductId).distinct().toList();
            List<com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity> products = productRepo.findAllById(productIds);
            if (products.size() != productIds.size()) {
                throw ApiException.badRequest("One or more Products linked to SKUs not found");
            }
            java.util.Map<Long, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity> productMap = products.stream()
                    .collect(java.util.stream.Collectors.toMap(com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity::getId, java.util.function.Function.identity()));

            // Prepare grouping
            record ItemContext(CheckoutRequest.Item item, SkuEntity sku, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity product) {}

            java.util.Map<Long, List<ItemContext>> byShop = new java.util.HashMap<>();

            for (var it : req.items()) {
                SkuEntity sku = skuMap.get(it.skuId());
                if (sku == null) throw ApiException.notFound("SKU not found: " + it.skuId());

                if (!sku.getProductId().equals(it.productId())) {
                    throw ApiException.badRequest("skuId " + it.skuId() + " does not belong to productId " + it.productId());
                }

                var product = productMap.get(sku.getProductId());
                if (product == null) throw ApiException.notFound("Product not found: " + sku.getProductId());

                byShop.computeIfAbsent(product.getShopId(), k -> new ArrayList<>())
                        .add(new ItemContext(it, sku, product));
            }
            // --- OPTIMIZATION END ---

            // Calculate total subtotal for coupon validation
            long totalSubtotal = 0;
            for (var entry : byShop.entrySet()) {
                for (var ctx : entry.getValue()) {
                    totalSubtotal += ctx.sku.getPrice() * ctx.item.quantity();
                }
            }

            // Validate and apply coupon if provided
            Long discountAmount = 0L;
            if (req.couponCode() != null && !req.couponCode().isBlank()) {
                List<Long> uniqueProductIds = new ArrayList<>(productIds);

                var couponValidation = couponService.validateCoupon(
                        req.couponCode(),
                        userId,
                        totalSubtotal,
                        uniqueProductIds,
                        List.of()
                );

                if (!couponValidation.valid()) {
                    throw ApiException.badRequest("Invalid coupon: " + couponValidation.message());
                }

                discountAmount = couponValidation.discountAmount();
            }

            List<OrderResponse> responses = new ArrayList<>();
            long now = System.currentTimeMillis();
            int idx = 0;

            for (var entry : byShop.entrySet()) {
                Long shopId = entry.getKey();
                List<ItemContext> shopItems = entry.getValue();
                idx++;

                String orderCode = "OD" + now + "_" + userId + "_" + shopId + "_" + idx;

                com.example.ecommerce.ecommerce_backend.domain.order.Order domainOrder = new com.example.ecommerce.ecommerce_backend.domain.order.Order(
                        orderCode,
                        userId,
                        shopId,
                        OrderStatus.SUBMITTED,
                        orderProperties.getCurrencyDefault(),
                        orderProperties.getShippingFeeDefault(),
                        0L,
                        new ArrayList<>()
                );

                for (var ctx : shopItems) {
                    reservationService.reserve(orderCode, ctx.sku.getId(), ctx.item.quantity());
                    domainOrder.addItem(ctx.product.getId(), ctx.sku.getId(), ctx.item.quantity(), ctx.sku.getPrice());
                }

                long subtotal = domainOrder.getItems().stream()
                        .mapToLong(com.example.ecommerce.ecommerce_backend.domain.order.OrderItem::getTotalPrice)
                        .sum();

                long shopDiscount = 0;
                if (discountAmount > 0 && byShop.size() > 1) {
                    if (totalSubtotal > 0) {
                        shopDiscount = (discountAmount * subtotal) / totalSubtotal;
                    }
                } else if (discountAmount > 0) {
                    shopDiscount = discountAmount;
                }

                domainOrder.applyDiscount(shopDiscount);

                if ("COD".equalsIgnoreCase(req.paymentMethod())) {
                    reservationService.commit(orderCode);
                    domainOrder.transitionTo(OrderStatus.PROCESSING);
                } else {
                    domainOrder.markAsPaymentPending();
                }

                OrderEntity o = new OrderEntity();
                o.setOrderCode(orderCode);
                o.setUserId(userId);
                o.setShopId(shopId);
                o.setAddressId(req.addressId());
                o.setPaymentMethod(req.paymentMethod());
                o.setNote(req.note());
                o.setCouponCode(req.couponCode());
                o.setCurrency(domainOrder.getCurrency());
                o.setShippingFee(domainOrder.getShippingFee());

                orderMapper.updateEntity(domainOrder, o);
                orderRepo.save(o);

                var itemEntities = orderMapper.toItemEntities(domainOrder, o.getId());
                itemRepo.saveAll(itemEntities);

                if (req.couponCode() != null && !req.couponCode().isBlank() && shopDiscount > 0 && idx == 1) {
                    couponService.applyCoupon(req.couponCode(), userId, o.getId(), discountAmount);
                }
                
                // Log initial order creation in history
                orderHistoryService.recordSystemChange(o.getId(), null, OrderStatus.SUBMITTED.name(), "Order created");
                if (domainOrder.getStatus() != OrderStatus.SUBMITTED) {
                    orderHistoryService.recordSystemChange(o.getId(), OrderStatus.SUBMITTED.name(), 
                        domainOrder.getStatus().name(), 
                        "COD".equalsIgnoreCase(req.paymentMethod()) ? "COD payment - auto confirmed" : "Payment pending");
                }

                List<OrderResponse.Item> respItems = itemEntities.stream()
                    .map(item -> {
                        var p = productMap.get(item.getProductId());
                        return new OrderResponse.Item(item.getProductId(), p.getName(), p.getMainImageUrl(), item.getSkuId(), item.getQuantity(), item.getUnitPrice(), item.getTotalPrice());
                    })
                    .collect(Collectors.toList());

                com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity addr = addressRepo.findById(o.getAddressId()).orElse(null);
                String fullAddress = (addr != null) ? String.format("%s, %s", addr.getWard(), addr.getProvince()) : "Unknown Address";

                responses.add(new OrderResponse(
                        orderCode,
                        o.getStatus(),
                        domainOrder.getTotalAmount(),
                        o.getCurrency(),
                        o.getCreatedAt(),
                        o.getPaymentMethod(),
                        o.getShippingFee(),
                        o.getDiscountAmount(),
                        o.getNote(),
                        o.getAddressId(),
                        addr != null ? addr.getReceiverName() : "Unknown",
                        addr != null ? addr.getReceiverPhone() : "Unknown",
                        fullAddress,
                        respItems
                ));
            }

            try {
                idempotencyService.complete("order.checkout", idemKey, 200, om.writeValueAsString(responses));
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                 throw ApiException.internalError("Failed to serialize order response");
            }

            return responses;

        } catch (ApiException e) {
            throw e; // Rethrow expected API exceptions
        } catch (Throwable e) {
            e.printStackTrace(); // Log stack trace
            throw ApiException.internalError("Checkout failed: " + e.getMessage() + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<OrderResponse> list(Long userId, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<OrderEntity> orders = orderRepo.findByUserId(userId, pageable);
        List<OrderEntity> orderList = orders.getContent();
        if (orderList.isEmpty()) {
            return new org.springframework.data.domain.PageImpl<>(List.of(), pageable, orders.getTotalElements());
        }

        List<Long> orderIds = orderList.stream().map(OrderEntity::getId).toList();
        List<com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity> allItems =
                itemRepo.findByOrderIdIn(orderIds);
        java.util.Map<Long, List<com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity>> itemsByOrder =
                allItems.stream().collect(Collectors.groupingBy(
                        com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity::getOrderId));

        java.util.Set<Long> productIds = allItems.stream()
                .map(com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity::getProductId)
                .collect(Collectors.toSet());
        java.util.Map<Long, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity> productMap =
                productRepo.findAllById(productIds).stream()
                        .collect(Collectors.toMap(
                                com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity::getId,
                                java.util.function.Function.identity()));

        java.util.Set<Long> addressIds = orderList.stream()
                .map(OrderEntity::getAddressId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        java.util.Map<Long, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity> addressMap =
                addressRepo.findAllById(addressIds).stream()
                        .collect(Collectors.toMap(
                                com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity::getId,
                                java.util.function.Function.identity()));

        List<OrderResponse> content = orderList.stream().map(o -> {
            var items = itemsByOrder.getOrDefault(o.getId(), List.of());
            List<OrderResponse.Item> respItems = items.stream()
                    .map(i -> {
                        var p = productMap.get(i.getProductId());
                        return new OrderResponse.Item(
                                i.getProductId(),
                                p != null ? p.getName() : "Unknown",
                                p != null ? p.getMainImageUrl() : null,
                                i.getSkuId(),
                                i.getQuantity(),
                                i.getUnitPrice(),
                                i.getTotalPrice()
                        );
                    })
                    .collect(Collectors.toList());

            var addr = o.getAddressId() != null ? addressMap.get(o.getAddressId()) : null;
            String fullAddress = (addr != null)
                    ? String.format("%s, %s", addr.getWard(), addr.getProvince())
                    : "Unknown Address";

            return new OrderResponse(
                    o.getOrderCode(),
                    o.getStatus(),
                    o.getTotalAmount(),
                    o.getCurrency(),
                    o.getCreatedAt(),
                    o.getPaymentMethod(),
                    o.getShippingFee(),
                    o.getDiscountAmount(),
                    o.getNote(),
                    o.getAddressId(),
                    addr != null ? addr.getReceiverName() : "Unknown",
                    addr != null ? addr.getReceiverPhone() : "Unknown",
                    fullAddress,
                    respItems
            );
        }).collect(Collectors.toList());

        return new org.springframework.data.domain.PageImpl<>(content, pageable, orders.getTotalElements());
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long userId, String orderCode) {
        OrderEntity o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");

        var items = itemRepo.findByOrderId(o.getId());
        var pIds = items.stream().map(com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity::getProductId).distinct().toList();
        var pMap = productRepo.findAllById(pIds).stream().collect(java.util.stream.Collectors.toMap(com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity::getId, java.util.function.Function.identity()));
        
        List<OrderResponse.Item> respItems = new ArrayList<>();
        for (var i : items) {
            var p = pMap.get(i.getProductId());
            respItems.add(new OrderResponse.Item(i.getProductId(), p != null ? p.getName() : "Unknown", p != null ? p.getMainImageUrl() : null, i.getSkuId(), i.getQuantity(), i.getUnitPrice(), i.getTotalPrice()));
        }

        System.out.println("DEBUG: Order " + orderCode + " has addressId: " + o.getAddressId());
        com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity addr = addressRepo.findById(o.getAddressId()).orElse(null);
        System.out.println("DEBUG: Found address: " + (addr != null ? addr.getId() : "NULL"));

        String fullAddress = (addr != null) ? String.format("%s, %s", addr.getWard(), addr.getProvince()) : "Unknown Address";

        return new OrderResponse(
                o.getOrderCode(),
                o.getStatus(),
                o.getTotalAmount(),
                o.getCurrency(),
                o.getCreatedAt(),
                o.getPaymentMethod(),
                o.getShippingFee(),
                o.getDiscountAmount(),
                o.getNote(),
                o.getAddressId(),
                addr != null ? addr.getReceiverName() : "Unknown",
                addr != null ? addr.getReceiverPhone() : "Unknown",
                fullAddress,
                respItems
        );
    }



    @Transactional
    public void cancel(Long userId, String orderCode) {
        OrderEntity o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");

        String previousStatus = o.getStatus();
        
        // Targeted improvement: Move invariants to domain layer
        // Load items for the domain aggregate (even if not strictly needed for cancelByUser, good practice)
        var itemEntities = itemRepo.findByOrderId(o.getId());
        var domainOrder = orderMapper.toDomain(o, itemEntities);
        
        domainOrder.cancelByUser();
        
        orderMapper.updateEntity(domainOrder, o);
        orderRepo.save(o);
        
        // Log status change
        orderHistoryService.recordBuyerChange(o.getId(), previousStatus, OrderStatus.CANCELLED.name(), 
            userId, "Cancelled by buyer");

        reservationService.release(orderCode);
        reservationService.restore(orderCode);
    }
    
    /**
     * Request return for a delivered order
     */
    @Transactional
    public void requestReturn(Long userId, String orderCode) {
        OrderEntity o = orderRepo.findByOrderCode(orderCode)
                .orElseThrow(() -> ApiException.notFound("Order not found"));
        if (!o.getUserId().equals(userId)) throw ApiException.notFound("Order not found");
        
        String previousStatus = o.getStatus();
        
        var itemEntities = itemRepo.findByOrderId(o.getId());
        var domainOrder = orderMapper.toDomain(o, itemEntities);
        
        domainOrder.requestReturn();
        
        orderMapper.updateEntity(domainOrder, o);
        orderRepo.save(o);
        
        // Log status change
        orderHistoryService.recordBuyerChange(o.getId(), previousStatus, OrderStatus.RETURN_REQUESTED.name(), 
            userId, "Return requested by buyer");
    }
}
