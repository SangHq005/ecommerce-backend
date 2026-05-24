package com.example.ecommerce.ecommerce_backend.application.service.cart;

import com.example.ecommerce.ecommerce_backend.api.dto.cart.AddToCartRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.cart.CartResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.api.exception.InsufficientStockException;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CartItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CartItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
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
    private final SkuJpaRepository skuRepo;
    private final ProductJpaRepository productRepo;
    private final SellerShopJpaRepository shopRepo;

    public CartService(CartItemJpaRepository cartRepo, SkuJpaRepository skuRepo, ProductJpaRepository productRepo, SellerShopJpaRepository shopRepo) {
        this.cartRepo = cartRepo;
        this.skuRepo = skuRepo;
        this.productRepo = productRepo;
        this.shopRepo = shopRepo;
    }

    @Transactional
    public void addToCart(Long userId, AddToCartRequest req) {
        SkuEntity sku = skuRepo.findById(req.skuId())
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

    /**
     * Add item to cart with enhanced validation and stock reservation
     * Requirements: 7.2, 7.3, 7.4
     * 
     * @param userId The user ID
     * @param req The add to cart request
     * @return The cart item ID
     * @throws ApiException if SKU not found or inactive
     * @throws InsufficientStockException if stock unavailable
     */
    @Transactional
    public Long addItem(Long userId, AddToCartRequest req) {
        // Validate SKU exists and is active
        SkuEntity sku = skuRepo.findById(req.skuId())
                .orElseThrow(() -> ApiException.notFound("SKU not found"));
        
        if (!sku.isActive()) {
            throw ApiException.badRequest("SKU is not active");
        }
        
        // Calculate available stock
        int availableStock = sku.getStockOnHand() - sku.getReservedStock();
        
        // Check if we have an existing cart item for this SKU
        CartItemEntity item = cartRepo.findByUserIdAndSkuId(userId, req.skuId())
                .orElse(null);
        
        int requestedQuantity = req.quantity();
        int newTotalQuantity = requestedQuantity;
        
        if (item != null) {
            // Update existing cart item
            newTotalQuantity = item.getQuantity() + requestedQuantity;
            
            // Check available stock >= requested quantity
            if (newTotalQuantity > availableStock) {
                throw new InsufficientStockException(req.skuId(), newTotalQuantity, availableStock);
            }
            
            item.setQuantity(newTotalQuantity);
            cartRepo.save(item);
            
            // Reserve stock for the additional quantity
            sku.setReservedStock(sku.getReservedStock() + requestedQuantity);
            skuRepo.save(sku);
            
            return item.getId();
        } else {
            // Create new cart item
            
            // Check available stock >= requested quantity
            if (requestedQuantity > availableStock) {
                throw new InsufficientStockException(req.skuId(), requestedQuantity, availableStock);
            }
            
            ProductEntity product = productRepo.findById(sku.getProductId())
                    .orElseThrow(() -> ApiException.notFound("Product not found"));
            
            item = new CartItemEntity();
            item.setUserId(userId);
            item.setSkuId(req.skuId());
            item.setProductId(product.getId());
            item.setShopId(product.getShopId());
            item.setQuantity(requestedQuantity);
            CartItemEntity savedItem = cartRepo.save(item);
            
            // Reserve stock for cart item
            sku.setReservedStock(sku.getReservedStock() + requestedQuantity);
            skuRepo.save(sku);
            
            return savedItem.getId();
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
                    sku.getOptionSignature(),
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

        SkuEntity sku = skuRepo.findById(item.getSkuId())
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
