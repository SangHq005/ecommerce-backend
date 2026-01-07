package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.cart.AddToCartRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.cart.CartResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CartItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductSkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CartItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductSkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemJpaRepository cartRepo;
    private final ProductSkuJpaRepository skuRepo;
    private final ProductJpaRepository productRepo;
    private final SellerShopJpaRepository shopRepo;

    public CartService(CartItemJpaRepository cartRepo, ProductSkuJpaRepository skuRepo, ProductJpaRepository productRepo, SellerShopJpaRepository shopRepo) {
        this.cartRepo = cartRepo;
        this.skuRepo = skuRepo;
        this.productRepo = productRepo;
        this.shopRepo = shopRepo;
    }

    @Transactional
    public void addToCart(Long userId, AddToCartRequest req) {
        ProductSkuEntity sku = skuRepo.findById(req.skuId())
                .orElseThrow(() -> ApiException.notFound("SKU not found"));
        
        ProductEntity product = productRepo.findById(sku.getProductId())
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        CartItemEntity item = cartRepo.findByUserIdAndSkuId(userId, req.skuId())
                .orElse(null);

        if (item != null) {
            int newQty = item.getQuantity() + req.quantity();
            // simple check stock
            if (newQty > (sku.getStockOnHand() - sku.getReservedStock())) {
                throw ApiException.badRequest("Exceeds available stock");
            }
            item.setQuantity(newQty);
            cartRepo.save(item);
        } else {
            if (req.quantity() > (sku.getStockOnHand() - sku.getReservedStock())) {
                throw ApiException.badRequest("Exceeds available stock");
            }
            item = new CartItemEntity();
            item.setUserId(userId);
            item.setSkuId(req.skuId());
            item.setProductId(product.getId());
            item.setShopId(product.getShopId());
            item.setQuantity(req.quantity());
            cartRepo.save(item);
        }
    }

    @Transactional(readOnly = true)
    public CartResponse getMyCart(Long userId) {
        List<CartItemEntity> items = cartRepo.findByUserId(userId);
        if (items.isEmpty()) {
            return new CartResponse(List.of(), 0);
        }

        Map<Long, List<CartItemEntity>> byShop = items.stream()
                .collect(Collectors.groupingBy(CartItemEntity::getShopId));

        List<CartResponse.CartGroup> groups = new ArrayList<>();
        long totalCount = 0;

        for (var entry : byShop.entrySet()) {
            Long shopId = entry.getKey();
            String shopName = shopRepo.findById(shopId).map(s -> s.getShopName()).orElse("Unknown Shop");
            List<CartItemEntity> shopItems = entry.getValue();

            List<CartResponse.CartItem> dtos = new ArrayList<>();
            for (var i : shopItems) {
                // In production, use bulk fetch. Here doing N+1 for simplicity or assume L2 cache
                var skuOpt = skuRepo.findById(i.getSkuId());
                var productOpt = productRepo.findById(i.getProductId());
                
                if (skuOpt.isEmpty() || productOpt.isEmpty()) continue;

                var sku = skuOpt.get();
                var p = productOpt.get();

                dtos.add(new CartResponse.CartItem(
                    i.getId(),
                    p.getId(),
                    p.getName(),
                    sku.getId(),
                    "Standard", // Variant name logic omitted
                    p.getMainImageUrl(),
                    sku.getPrice(),
                    i.getQuantity(),
                    sku.getStockOnHand() - sku.getReservedStock() // Max stock available
                ));
                totalCount += i.getQuantity();
            }
            if (!dtos.isEmpty()) {
                groups.add(new CartResponse.CartGroup(shopId, shopName, dtos));
            }
        }

        return new CartResponse(groups, totalCount);
    }
    
    @Transactional
    public void updateQuantity(Long userId, Long itemId, int quantity) {
        if (quantity <= 0) {
            deleteItem(userId, itemId);
            return;
        }
        CartItemEntity item = cartRepo.findById(itemId)
                .orElseThrow(() -> ApiException.notFound("Item not found"));
        
        if (!item.getUserId().equals(userId)) throw ApiException.notFound("Item not found"); // security

        ProductSkuEntity sku = skuRepo.findById(item.getSkuId())
                .orElseThrow(() -> ApiException.notFound("SKU not found"));
        
        if (quantity > (sku.getStockOnHand() - sku.getReservedStock())) {
             throw ApiException.badRequest("Max stock available is " + (sku.getStockOnHand() - sku.getReservedStock()));
        }

        item.setQuantity(quantity);
        cartRepo.save(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        CartItemEntity item = cartRepo.findById(itemId).orElse(null);
        if (item != null && item.getUserId().equals(userId)) {
            cartRepo.delete(item);
        }
    }
}
