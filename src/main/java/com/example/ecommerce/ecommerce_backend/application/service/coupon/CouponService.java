package com.example.ecommerce.ecommerce_backend.application.service.coupon;

import com.example.ecommerce.ecommerce_backend.api.dto.coupon.CouponValidationResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponStatus;
import com.example.ecommerce.ecommerce_backend.domain.promotion.CouponType;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CouponUsageEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CouponJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CouponUsageJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class CouponService {

    private static final Logger log = LoggerFactory.getLogger(CouponService.class);

    private final CouponJpaRepository couponRepo;
    private final CouponUsageJpaRepository usageRepo;

    public CouponService(CouponJpaRepository couponRepo, CouponUsageJpaRepository usageRepo) {
        this.couponRepo = couponRepo;
        this.usageRepo = usageRepo;
    }

    /**
     * Validate coupon and calculate discount
     */
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(
            String couponCode,
            Long userId,
            Long orderTotal,
            List<Long> productIds,
            List<Long> categoryIds
    ) {
        log.info("Validating coupon {} for user {} with order total {}", couponCode, userId, orderTotal);

        // Find coupon
        CouponEntity coupon = couponRepo.findByCode(couponCode.toUpperCase())
                .orElse(null);

        if (coupon == null) {
            return CouponValidationResponse.invalid("Coupon code not found");
        }

        // Check status
        if (coupon.getStatus() != CouponStatus.ACTIVE) {
            return CouponValidationResponse.invalid("Coupon is not active");
        }

        // Check date validity
        Instant now = Instant.now();
        if (now.isBefore(coupon.getStartDate())) {
            return CouponValidationResponse.invalid("Coupon is not yet valid");
        }
        if (now.isAfter(coupon.getEndDate())) {
            return CouponValidationResponse.invalid("Coupon has expired");
        }

        // Check usage limit
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            return CouponValidationResponse.invalid("Coupon usage limit reached");
        }

        // Check per-user usage limit
        if (coupon.getUsageLimitPerUser() != null) {
            long userUsageCount = usageRepo.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsageCount >= coupon.getUsageLimitPerUser()) {
                return CouponValidationResponse.invalid("You have already used this coupon the maximum number of times");
            }
        }

        // Check minimum order amount
        if (coupon.getMinOrderAmount() != null && orderTotal < coupon.getMinOrderAmount()) {
            return CouponValidationResponse.invalid(
                    String.format("Minimum order amount of %d VND required", coupon.getMinOrderAmount())
            );
        }

        // Check user restriction
        if (coupon.getApplicableUserIds() != null && !coupon.getApplicableUserIds().isEmpty()) {
            if (!coupon.getApplicableUserIds().contains(userId)) {
                return CouponValidationResponse.invalid("This coupon is not available for your account");
            }
        }

        // Check product restriction
        if (coupon.getApplicableProductIds() != null && !coupon.getApplicableProductIds().isEmpty()) {
            if (productIds == null || productIds.isEmpty()) {
                return CouponValidationResponse.invalid("This coupon applies to specific products only");
            }
            boolean hasApplicableProduct = productIds.stream()
                    .anyMatch(coupon.getApplicableProductIds()::contains);
            if (!hasApplicableProduct) {
                return CouponValidationResponse.invalid("This coupon does not apply to products in your cart");
            }
        }

        // Check category restriction
        if (coupon.getApplicableCategoryIds() != null && !coupon.getApplicableCategoryIds().isEmpty()) {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return CouponValidationResponse.invalid("This coupon applies to specific categories only");
            }
            boolean hasApplicableCategory = categoryIds.stream()
                    .anyMatch(coupon.getApplicableCategoryIds()::contains);
            if (!hasApplicableCategory) {
                return CouponValidationResponse.invalid("This coupon does not apply to categories in your cart");
            }
        }

        // Calculate discount
        Long discountAmount = calculateDiscount(coupon, orderTotal);

        log.info("Coupon {} validated successfully, discount amount: {}", couponCode, discountAmount);
        return CouponValidationResponse.valid(discountAmount, coupon.getCode(), coupon.getName());
    }

    /**
     * Calculate discount amount based on coupon type
     */
    private Long calculateDiscount(CouponEntity coupon, Long orderTotal) {
        Long discount = 0L;

        switch (coupon.getType()) {
            case PERCENTAGE:
                discount = (orderTotal * coupon.getDiscountValue()) / 100;
                // Apply max discount cap if specified
                if (coupon.getMaxDiscountAmount() != null && discount > coupon.getMaxDiscountAmount()) {
                    discount = coupon.getMaxDiscountAmount();
                }
                break;

            case FIXED_AMOUNT:
                discount = coupon.getDiscountValue();
                // Discount cannot exceed order total
                if (discount > orderTotal) {
                    discount = orderTotal;
                }
                break;

            case FREE_SHIPPING:
                // Shipping cost should be passed separately
                // For now, return a fixed value
                discount = coupon.getDiscountValue();
                break;
        }

        return discount;
    }

    /**
     * Apply coupon to order (record usage)
     */
    @Transactional
    public void applyCoupon(String couponCode, Long userId, Long orderId, Long discountAmount) {
        log.info("Applying coupon {} to order {} for user {}", couponCode, orderId, userId);

        CouponEntity coupon = couponRepo.findByCode(couponCode.toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));

        // Check if already applied to this order
        if (usageRepo.existsByOrderId(orderId)) {
            throw ApiException.conflict("A coupon has already been applied to this order");
        }

        // Record usage
        CouponUsageEntity usage = new CouponUsageEntity();
        usage.setCouponId(coupon.getId());
        usage.setUserId(userId);
        usage.setOrderId(orderId);
        usage.setDiscountAmount(discountAmount);
        usageRepo.save(usage);

        // Increment usage count
        coupon.incrementUsageCount();
        couponRepo.save(coupon);

        log.info("Coupon {} applied successfully to order {}", couponCode, orderId);
    }

    /**
     * Find auto-apply coupons for user
     */
    @Transactional(readOnly = true)
    public CouponEntity findBestAutoApplyCoupon(
            Long userId,
            Long orderTotal,
            List<Long> productIds,
            List<Long> categoryIds
    ) {
        log.info("Finding best auto-apply coupon for user {} with order total {}", userId, orderTotal);

        List<CouponEntity> autoApplyCoupons = couponRepo.findAutoApplyCoupons(CouponStatus.ACTIVE);

        CouponEntity bestCoupon = null;
        Long maxDiscount = 0L;

        for (CouponEntity coupon : autoApplyCoupons) {
            CouponValidationResponse validation = validateCoupon(
                    coupon.getCode(),
                    userId,
                    orderTotal,
                    productIds,
                    categoryIds
            );

            if (validation.valid() && validation.discountAmount() > maxDiscount) {
                maxDiscount = validation.discountAmount();
                bestCoupon = coupon;
            }
        }

        if (bestCoupon != null) {
            log.info("Found best auto-apply coupon: {} with discount {}", bestCoupon.getCode(), maxDiscount);
        } else {
            log.info("No auto-apply coupon found");
        }

        return bestCoupon;
    }

    /**
     * Get all active coupons
     */
    @Transactional(readOnly = true)
    public List<CouponEntity> getActiveCoupons() {
        return couponRepo.findActiveValidCoupons(CouponStatus.ACTIVE);
    }

    /**
     * Get coupon by code
     */
    @Transactional(readOnly = true)
    public CouponEntity getCouponByCode(String code) {
        return couponRepo.findByCode(code.toUpperCase())
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));
    }

    /**
     * Get user's coupon usage history
     */
    @Transactional(readOnly = true)
    public List<CouponUsageEntity> getUserCouponUsage(Long userId) {
        return usageRepo.findByUserId(userId);
    }

    /**
     * Create new coupon (Admin)
     */
    @Transactional
    public CouponEntity createCoupon(CouponEntity coupon) {
        log.info("Creating new coupon: {}", coupon.getCode());

        // Check if code already exists
        if (couponRepo.findByCode(coupon.getCode().toUpperCase()).isPresent()) {
            throw ApiException.conflict("Coupon code already exists");
        }

        // Validate dates
        if (coupon.getEndDate().isBefore(coupon.getStartDate())) {
            throw ApiException.badRequest("End date must be after start date");
        }

        // Validate percentage discount
        if (coupon.getType() == CouponType.PERCENTAGE) {
            if (coupon.getDiscountValue() > 100) {
                throw ApiException.badRequest("Percentage discount cannot exceed 100%");
            }
        }

        coupon.setCode(coupon.getCode().toUpperCase());
        coupon.setStatus(CouponStatus.ACTIVE);
        coupon.setUsageCount(0);

        CouponEntity saved = couponRepo.save(coupon);
        log.info("Coupon created successfully: {}", saved.getCode());
        return saved;
    }

    /**
     * Update coupon (Admin)
     */
    @Transactional
    public CouponEntity updateCoupon(Long couponId, CouponEntity updates) {
        log.info("Updating coupon: {}", couponId);

        CouponEntity existing = couponRepo.findById(couponId)
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));

        // Cannot change code if already used
        if (!existing.getCode().equals(updates.getCode()) && existing.getUsageCount() > 0) {
            throw ApiException.badRequest("Cannot change code of a coupon that has been used");
        }

        existing.setCode(updates.getCode().toUpperCase());
        existing.setName(updates.getName());
        existing.setDescription(updates.getDescription());
        existing.setType(updates.getType());
        existing.setDiscountValue(updates.getDiscountValue());
        existing.setMaxDiscountAmount(updates.getMaxDiscountAmount());
        existing.setMinOrderAmount(updates.getMinOrderAmount());
        existing.setStartDate(updates.getStartDate());
        existing.setEndDate(updates.getEndDate());
        existing.setUsageLimit(updates.getUsageLimit());
        existing.setUsageLimitPerUser(updates.getUsageLimitPerUser());
        existing.setAutoApply(updates.getAutoApply());
        existing.setApplicableProductIds(updates.getApplicableProductIds());
        existing.setApplicableCategoryIds(updates.getApplicableCategoryIds());
        existing.setApplicableUserIds(updates.getApplicableUserIds());
        if (updates.getStatus() != null) {
            existing.setStatus(updates.getStatus());
        }

        CouponEntity saved = couponRepo.save(existing);
        log.info("Coupon updated successfully: {}", saved.getCode());
        return saved;
    }

    /**
     * Delete coupon (Admin)
     */
    @Transactional
    public void deleteCoupon(Long couponId) {
        log.info("Deleting coupon: {}", couponId);

        CouponEntity coupon = couponRepo.findById(couponId)
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));

        // Cannot delete if already used
        if (coupon.getUsageCount() > 0) {
            throw ApiException.badRequest("Cannot delete a coupon that has been used. Set status to INACTIVE instead.");
        }

        couponRepo.delete(coupon);
        log.info("Coupon deleted successfully");
    }

    @Transactional(readOnly = true)
    public Page<CouponEntity> adminSearch(CouponStatus status, Boolean autoApply, String q, Pageable pageable) {
        Specification<CouponEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (status != null) {
                predicates.getExpressions().add(cb.equal(root.get("status"), status));
            }
            if (autoApply != null) {
                predicates.getExpressions().add(cb.equal(root.get("autoApply"), autoApply));
            }
            if (q != null && !q.isBlank()) {
                String like = "%" + q.trim().toLowerCase() + "%";
                predicates.getExpressions().add(cb.or(
                        cb.like(cb.lower(root.get("code")), like),
                        cb.like(cb.lower(root.get("name")), like)
                ));
            }
            return predicates;
        };

        return couponRepo.findAll(spec, pageable);
    }

    @Transactional
    public CouponEntity updateStatus(Long couponId, CouponStatus status) {
        CouponEntity coupon = couponRepo.findById(couponId)
                .orElseThrow(() -> ApiException.notFound("Coupon not found"));
        coupon.setStatus(status);
        return couponRepo.save(coupon);
    }
}
