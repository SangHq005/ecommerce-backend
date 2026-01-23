package com.example.ecommerce.ecommerce_backend.application.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductImageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductImageJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;

/**
 * Service for calculating product listing quality score.
 * Implements Shopee-like quality assessment for product listings.
 */
@Service
public class ProductQualityService {

    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository imageRepo;
    private final SkuJpaRepository skuRepo;

    public ProductQualityService(
            ProductJpaRepository productRepo,
            ProductImageJpaRepository imageRepo,
            SkuJpaRepository skuRepo
    ) {
        this.productRepo = productRepo;
        this.imageRepo = imageRepo;
        this.skuRepo = skuRepo;
    }

    /**
     * Quality check result with detailed breakdown
     */
    public record QualityCheckResult(
            int score,
            String grade,
            List<QualityItem> items,
            List<String> improvements
    ) {
        public static String gradeFromScore(int score) {
            if (score >= 90) return "EXCELLENT";
            if (score >= 70) return "GOOD";
            if (score >= 50) return "FAIR";
            return "NEEDS_IMPROVEMENT";
        }
    }

    public record QualityItem(
            String name,
            String status,
            int points,
            int maxPoints,
            String suggestion
    ) {}

    /**
     * Calculate quality score for a product
     */
    @Transactional(readOnly = true)
    public QualityCheckResult checkQuality(Long productId) {
        ProductEntity product = productRepo.findById(productId).orElseThrow();
        List<ProductImageEntity> images = imageRepo.findByProductIdOrderBySortOrderAsc(productId);
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(productId);

        List<QualityItem> items = new ArrayList<>();
        List<String> improvements = new ArrayList<>();
        int totalScore = 0;

        // 1. Product Name (15 points max)
        int nameScore = checkName(product.getName());
        items.add(new QualityItem(
                "Tên sản phẩm",
                nameScore >= 12 ? "GOOD" : (nameScore >= 8 ? "FAIR" : "POOR"),
                nameScore,
                15,
                nameScore < 12 ? "Tên nên từ 30-100 ký tự, mô tả rõ sản phẩm" : null
        ));
        totalScore += nameScore;
        if (nameScore < 12) improvements.add("Cải thiện tên sản phẩm: nên từ 30-100 ký tự");

        // 2. Description (20 points max)
        int descScore = checkDescription(product.getDescription());
        items.add(new QualityItem(
                "Mô tả sản phẩm",
                descScore >= 16 ? "GOOD" : (descScore >= 10 ? "FAIR" : "POOR"),
                descScore,
                20,
                descScore < 16 ? "Mô tả chi tiết giúp khách hàng hiểu rõ sản phẩm" : null
        ));
        totalScore += descScore;
        if (descScore < 16) improvements.add("Thêm mô tả chi tiết về sản phẩm (tối thiểu 100 ký tự)");

        // 3. Images (25 points max)
        int imageScore = checkImages(images);
        items.add(new QualityItem(
                "Hình ảnh",
                imageScore >= 20 ? "GOOD" : (imageScore >= 12 ? "FAIR" : "POOR"),
                imageScore,
                25,
                imageScore < 20 ? "Thêm 4-8 hình ảnh rõ nét, đa góc độ" : null
        ));
        totalScore += imageScore;
        if (imageScore < 20) improvements.add("Thêm hình ảnh chất lượng cao (khuyến nghị 4-8 ảnh)");

        // 4. SKU/Variants (15 points max)
        int skuScore = checkSkus(skus);
        items.add(new QualityItem(
                "Phân loại hàng",
                skuScore >= 12 ? "GOOD" : (skuScore >= 8 ? "FAIR" : "POOR"),
                skuScore,
                15,
                skuScore < 12 ? "Thêm nhiều phân loại với giá và tồn kho rõ ràng" : null
        ));
        totalScore += skuScore;
        if (skuScore < 12) improvements.add("Thêm phân loại hàng với giá cụ thể");

        // 5. Pricing (10 points max)
        int priceScore = checkPricing(product, skus);
        items.add(new QualityItem(
                "Giá bán",
                priceScore >= 8 ? "GOOD" : (priceScore >= 5 ? "FAIR" : "POOR"),
                priceScore,
                10,
                priceScore < 8 ? "Đặt giá cạnh tranh với giá gốc để hiển thị khuyến mãi" : null
        ));
        totalScore += priceScore;
        if (priceScore < 8) improvements.add("Thêm giá gốc để hiển thị khuyến mãi");

        // 6. Category (5 points max)
        int categoryScore = product.getCategoryId() != null ? 5 : 0;
        items.add(new QualityItem(
                "Danh mục",
                categoryScore == 5 ? "GOOD" : "POOR",
                categoryScore,
                5,
                categoryScore < 5 ? "Chọn danh mục phù hợp" : null
        ));
        totalScore += categoryScore;

        // 7. Weight/Shipping (5 points max)
        int shippingScore = checkShipping(product);
        items.add(new QualityItem(
                "Thông tin vận chuyển",
                shippingScore >= 4 ? "GOOD" : (shippingScore >= 2 ? "FAIR" : "POOR"),
                shippingScore,
                5,
                shippingScore < 4 ? "Thêm cân nặng để tính phí vận chuyển chính xác" : null
        ));
        totalScore += shippingScore;
        if (shippingScore < 4) improvements.add("Thêm cân nặng sản phẩm");

        // 8. Brand (5 points max)
        int brandScore = product.getBrandId() != null ? 5 : 0;
        items.add(new QualityItem(
                "Thương hiệu",
                brandScore == 5 ? "GOOD" : "FAIR",
                brandScore,
                5,
                brandScore < 5 ? "Chọn thương hiệu nếu có" : null
        ));
        totalScore += brandScore;

        String grade = QualityCheckResult.gradeFromScore(totalScore);
        return new QualityCheckResult(totalScore, grade, items, improvements);
    }

    /**
     * Update product quality score in database
     */
    @Transactional
    public int updateQualityScore(Long productId) {
        QualityCheckResult result = checkQuality(productId);
        ProductEntity product = productRepo.findById(productId).orElseThrow();
        product.setQualityScore(result.score());
        productRepo.save(product);
        return result.score();
    }

    // === Private Helper Methods ===

    private int checkName(String name) {
        if (name == null || name.isBlank()) return 0;
        int length = name.trim().length();
        if (length >= 30 && length <= 100) return 15;
        if (length >= 20 && length <= 120) return 12;
        if (length >= 10) return 8;
        return 4;
    }

    private int checkDescription(String description) {
        if (description == null || description.isBlank()) return 0;
        int length = description.trim().length();
        if (length >= 500) return 20;
        if (length >= 200) return 16;
        if (length >= 100) return 10;
        if (length >= 50) return 6;
        return 3;
    }

    private int checkImages(List<ProductImageEntity> images) {
        int count = images.size();
        if (count >= 6) return 25;
        if (count >= 4) return 20;
        if (count >= 3) return 15;
        if (count >= 2) return 10;
        if (count >= 1) return 5;
        return 0;
    }

    private int checkSkus(List<SkuEntity> skus) {
        if (skus.isEmpty()) return 0;
        
        int score = 5; // Base score for having at least 1 SKU
        
        // More SKUs = better
        if (skus.size() >= 4) score += 5;
        else if (skus.size() >= 2) score += 3;
        
        // All SKUs have stock
        boolean allHaveStock = skus.stream().allMatch(s -> s.getStockOnHand() > 0);
        if (allHaveStock) score += 3;
        
        // All SKUs are active
        boolean allActive = skus.stream().allMatch(SkuEntity::isActive);
        if (allActive) score += 2;
        
        return Math.min(score, 15);
    }

    private int checkPricing(ProductEntity product, List<SkuEntity> skus) {
        int score = 0;
        
        // Has base price
        if (product.getPrice() != null && product.getPrice() > 0) {
            score += 5;
        }
        
        // Has compare-at price (shows discount)
        if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
            score += 3;
        }
        
        // SKUs have proper pricing
        boolean skusHavePrices = skus.stream().allMatch(s -> s.getPrice() > 0);
        if (skusHavePrices) score += 2;
        
        return Math.min(score, 10);
    }

    private int checkShipping(ProductEntity product) {
        int score = 0;
        
        // Has weight
        if (product.getWeightGrams() != null && product.getWeightGrams() > 0) {
            score += 3;
        }
        
        // Has shipping type configured
        if (product.getShippingFeeType() != null && !product.getShippingFeeType().isBlank()) {
            score += 2;
        }
        
        return score;
    }
}
