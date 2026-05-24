package com.example.ecommerce.ecommerce_backend.application.service.user;

import com.example.ecommerce.ecommerce_backend.api.dto.wishlist.WishlistItemResponse;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.WishlistItemEntity;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.WishlistItemJpaRepository;import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;

import java.util.ArrayList;
import java.util.List;

import com.example.ecommerce.ecommerce_backend.application.service.recommendation.RecommendationService;
@Service
public class WishlistService {

    private static final Logger log = LoggerFactory.getLogger(WishlistService.class);

    private final WishlistItemJpaRepository wishlistRepo;
    private final ProductJpaRepository productRepo;
    private final SkuJpaRepository skuRepo;
    private final RecommendationService recommendationService;

    public WishlistService(
            WishlistItemJpaRepository wishlistRepo,
            ProductJpaRepository productRepo,
            SkuJpaRepository skuRepo,
            RecommendationService recommendationService
    ) {
        this.wishlistRepo = wishlistRepo;
        this.productRepo = productRepo;
        this.skuRepo = skuRepo;
        this.recommendationService = recommendationService;
    }

    /**
     * Add product to user's wishlist
     */
    @Transactional
    public WishlistItemResponse addToWishlist(Long userId, Long productId, String note) {
        log.info("Adding product {} to wishlist for user {}", productId, userId);

        // Validate product exists and is active
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw ApiException.badRequest("Product is not available");
        }

        // Check if already in wishlist
        if (wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            throw ApiException.conflict("Product already in wishlist");
        }

        // Create wishlist item
        WishlistItemEntity item = new WishlistItemEntity();
        item.setUserId(userId);
        item.setProductId(productId);
        item.setNote(note);
        item = wishlistRepo.save(item);

        // Track event for recommendations
        recommendationService.trackEvent(userId, productId, "WISHLIST_ADD");

        // Get min price
        Long minPrice = skuRepo.findMinPriceByProductId(productId);

        log.info("Product {} added to wishlist for user {}", productId, userId);
        return WishlistItemResponse.from(item, product, minPrice);
    }

    /**
     * Remove product from user's wishlist
     */
    @Transactional
    public void removeFromWishlist(Long userId, Long productId) {
        log.info("Removing product {} from wishlist for user {}", productId, userId);

        if (!wishlistRepo.existsByUserIdAndProductId(userId, productId)) {
            throw ApiException.notFound("Product not in wishlist");
        }

        wishlistRepo.deleteByUserIdAndProductId(userId, productId);
        log.info("Product {} removed from wishlist for user {}", productId, userId);
    }

    /**
     * Get all wishlist items for a user
     */
    @Transactional(readOnly = true)
    public List<WishlistItemResponse> getUserWishlist(Long userId) {
        log.info("Getting wishlist for user {}", userId);

        List<WishlistItemEntity> items = wishlistRepo.findByUserId(userId);
        List<WishlistItemResponse> responses = new ArrayList<>();

        for (WishlistItemEntity item : items) {
            ProductEntity product = productRepo.findById(item.getProductId()).orElse(null);
            if (product == null) {
                // Product was deleted, skip it
                continue;
            }

            Long minPrice = skuRepo.findMinPriceByProductId(item.getProductId());
            responses.add(WishlistItemResponse.from(item, product, minPrice));
        }

        log.info("Found {} wishlist items for user {}", responses.size(), userId);
        return responses;
    }

    /**
     * Check if product is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepo.existsByUserIdAndProductId(userId, productId);
    }

    /**
     * Get wishlist count for user
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(Long userId) {
        return wishlistRepo.countByUserId(userId);
    }

    /**
     * Get product IDs in user's wishlist
     */
    @Transactional(readOnly = true)
    public List<Long> getWishlistProductIds(Long userId) {
        return wishlistRepo.findProductIdsByUserId(userId);
    }

    /**
     * Clear user's entire wishlist
     */
    @Transactional
    public void clearWishlist(Long userId) {
        log.info("Clearing wishlist for user {}", userId);
        List<WishlistItemEntity> items = wishlistRepo.findByUserId(userId);
        wishlistRepo.deleteAll(items);
        log.info("Cleared {} items from wishlist for user {}", items.size(), userId);
    }
}
