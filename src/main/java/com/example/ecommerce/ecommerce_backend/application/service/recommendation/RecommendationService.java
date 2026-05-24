package com.example.ecommerce.ecommerce_backend.application.service.recommendation;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.UserCategoryAffinityDoc;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.document.UserEventDoc;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.UserCategoryAffinityMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.UserEventMongoRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final UserEventMongoRepository eventRepo;
    private final ProductJpaRepository productRepo;
    private final UserCategoryAffinityMongoRepository affinityRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final SkuJpaRepository skuRepo;

    public RecommendationService(
            UserEventMongoRepository eventRepo,
            ProductJpaRepository productRepo,
            UserCategoryAffinityMongoRepository affinityRepo,
            OrderItemJpaRepository orderItemRepo,
            SkuJpaRepository skuRepo
    ) {
        this.eventRepo = eventRepo;
        this.productRepo = productRepo;
        this.affinityRepo = affinityRepo;
        this.orderItemRepo = orderItemRepo;
        this.skuRepo = skuRepo;
    }

    public void trackEvent(Long userId, Long productId, String eventType) {
        UserEventDoc doc = new UserEventDoc();
        doc.setUserId(userId);
        doc.setProductId(productId);
        doc.setEventType(eventType);
        doc.setTimestamp(Instant.now());
        eventRepo.save(doc);

        // Update category affinity if user is authenticated
        if (userId != null) {
            productRepo.findById(productId).ifPresent(product -> {
                updateCategoryAffinity(userId, product.getCategoryId(), eventType);
            });
        }
    }

    public List<ProductEntity> getTrendingProducts() {
        // Top viewed in last 7 days
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<UserEventMongoRepository.ProductIdCount> top = eventRepo.findTopProductsByEventType("VIEW", sevenDaysAgo);

        List<ProductEntity> products = new ArrayList<>();
        for (var t : top) {
            productRepo.findById(t.get_id()).ifPresent(products::add);
        }
        return products;
    }

    /**
     * Get personalized recommendations based on user's viewing and purchase history
     */
    public List<ProductEntity> getPersonalizedRecommendations(Long userId, int limit) {
        log.info("Getting personalized recommendations for user: {}", userId);

        // Get user's top categories by affinity score
        List<UserCategoryAffinityDoc> topCategories = affinityRepo.findByUserIdOrderByScoreDesc(userId);

        if (topCategories.isEmpty()) {
            // Fallback to trending if no history
            log.info("No user history found, returning trending products");
            return getTrendingProducts().stream().limit(limit).toList();
        }

        // Get products from top 3 categories
        Set<ProductEntity> recommendations = new LinkedHashSet<>();
        for (UserCategoryAffinityDoc affinity : topCategories.stream().limit(3).toList()) {
            List<ProductEntity> categoryProducts = productRepo.findByCategoryIdAndStatus(
                    affinity.getCategoryId(),
                    "ACTIVE",
                    PageRequest.of(0, limit)
            );
            recommendations.addAll(categoryProducts);

            if (recommendations.size() >= limit) {
                break;
            }
        }

        // Remove products user already viewed recently
        Set<Long> recentlyViewedIds = getRecentlyViewedProductIds(userId, 30);
        List<ProductEntity> filtered = recommendations.stream()
                .filter(p -> !recentlyViewedIds.contains(p.getId()))
                .limit(limit)
                .toList();

        log.info("Found {} personalized recommendations for user {}", filtered.size(), userId);
        return filtered;
    }

    /**
     * Get similar products based on category, brand, and price range
     */
    public List<ProductEntity> getSimilarProducts(Long productId, int limit) {
        log.info("Getting similar products for product: {}", productId);

        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        // Find products with same category and brand
        List<ProductEntity> similar = productRepo.findByCategoryIdAndStatus(
                product.getCategoryId(),
                "ACTIVE",
                PageRequest.of(0, limit * 2) // Get more to filter
        );

        // Calculate similarity scores
        Map<Long, Double> scores = new HashMap<>();
        for (ProductEntity p : similar) {
            if (p.getId().equals(productId)) continue; // Skip same product

            double score = 0.0;

            // Same category: +40 points
            if (p.getCategoryId().equals(product.getCategoryId())) {
                score += 40;
            }

            // Same brand: +30 points
            if (p.getBrandId() != null && p.getBrandId().equals(product.getBrandId())) {
                score += 30;
            }

            // Similar price range (±20%): +20 points
            Long productMinPrice = skuRepo.findMinPriceByProductId(product.getId());
            Long pMinPrice = skuRepo.findMinPriceByProductId(p.getId());
            if (productMinPrice != null && pMinPrice != null) {
                double priceDiff = Math.abs(pMinPrice.doubleValue() - productMinPrice.doubleValue());
                double pricePercent = (priceDiff / productMinPrice.doubleValue()) * 100;
                if (pricePercent <= 20) {
                    score += 20;
                }
            }

            scores.put(p.getId(), score);
        }

        // Sort by score and return top N
        List<ProductEntity> result = similar.stream()
                .filter(p -> !p.getId().equals(productId))
                .sorted((p1, p2) -> Double.compare(
                        scores.getOrDefault(p2.getId(), 0.0),
                        scores.getOrDefault(p1.getId(), 0.0)
                ))
                .limit(limit)
                .toList();

        log.info("Found {} similar products for product {}", result.size(), productId);
        return result;
    }

    /**
     * Get frequently bought together products (co-purchase analysis)
     */
    public List<ProductEntity> getFrequentlyBoughtTogether(Long productId, int limit) {
        log.info("Getting frequently bought together for product: {}", productId);

        // Find orders containing this product
        List<OrderItemEntity> orderItems = orderItemRepo.findByProductId(productId);

        if (orderItems.isEmpty()) {
            return Collections.emptyList();
        }

        // Get all order IDs
        Set<Long> orderIds = orderItems.stream()
                .map(OrderItemEntity::getOrderId)
                .collect(Collectors.toSet());

        // Find other products in same orders
        Map<Long, Long> productCounts = new HashMap<>();
        for (Long orderId : orderIds) {
            List<OrderItemEntity> items = orderItemRepo.findByOrderId(orderId);
            for (OrderItemEntity item : items) {
                if (!item.getProductId().equals(productId)) {
                    productCounts.merge(item.getProductId(), 1L, Long::sum);
                }
            }
        }

        // Sort by frequency and get top N
        List<Long> topProductIds = productCounts.entrySet().stream()
                .filter(e -> e.getValue() >= 3) // Min 3 co-occurrences
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();

        List<ProductEntity> result = new ArrayList<>();
        for (Long id : topProductIds) {
            productRepo.findById(id).ifPresent(result::add);
        }

        log.info("Found {} frequently bought together products", result.size());
        return result;
    }

    /**
     * Update category affinity when user interacts with products
     */
    private void updateCategoryAffinity(Long userId, Long categoryId, String eventType) {
        if (categoryId == null) return;

        UserCategoryAffinityDoc affinity = affinityRepo
                .findByUserIdAndCategoryId(userId, categoryId)
                .orElseGet(() -> {
                    UserCategoryAffinityDoc newAffinity = new UserCategoryAffinityDoc();
                    newAffinity.setUserId(userId);
                    newAffinity.setCategoryId(categoryId);
                    return newAffinity;
                });

        if ("VIEW".equals(eventType)) {
            affinity.incrementView();
        } else if ("PURCHASE".equals(eventType)) {
            affinity.incrementPurchase();
        }

        affinityRepo.save(affinity);
        log.debug("Updated category affinity for user {} category {}", userId, categoryId);
    }

    /**
     * Get recently viewed product IDs for filtering
     */
    private Set<Long> getRecentlyViewedProductIds(Long userId, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        List<UserEventDoc> events = eventRepo.findByUserIdAndEventTypeAndTimestampAfter(
                userId, "VIEW", since
        );
        return events.stream()
                .map(UserEventDoc::getProductId)
                .collect(Collectors.toSet());
    }
}
