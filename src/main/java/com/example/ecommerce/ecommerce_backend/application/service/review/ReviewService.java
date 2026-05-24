package com.example.ecommerce.ecommerce_backend.application.service.review;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ReviewJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewJpaRepository reviewRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final ProductJpaRepository productRepo;

    public ReviewService(
            ReviewJpaRepository reviewRepo,
            OrderItemJpaRepository orderItemRepo,
            ProductJpaRepository productRepo
    ) {
        this.reviewRepo = reviewRepo;
        this.orderItemRepo = orderItemRepo;
        this.productRepo = productRepo;
    }

    @Transactional
    public ReviewEntity createReview(Long userId, Long productId, Integer rating, String comment, List<String> images, Long parentId) {
        // If it's a root review, validate rating
        if (parentId == null) {
            if (rating == null || rating < 1 || rating > 5) throw ApiException.badRequest("Rating must be between 1 and 5");
            // Users can now submit multiple reviews for the same product
        }

        // If it's a reply
        Long orderId = null;
        if (parentId != null) {
            // Verify parent exists
            ReviewEntity parent = reviewRepo.findById(parentId)
                    .orElseThrow(() -> ApiException.notFound("Parent review not found"));
            if (!parent.getProductId().equals(productId)) {
                throw ApiException.badRequest("Parent review does not belong to this product");
            }
        } else {
            // Root review logic continued
             List<String> statuses = List.of(
                    OrderStatus.PAID.name(),
                    OrderStatus.FULFILLED.name(),
                    OrderStatus.DELIVERED.name(),
                    OrderStatus.COMPLETED.name()
            );
            List<Long> orderIds = orderItemRepo.findPurchasedOrderIds(userId, productId, statuses, PageRequest.of(0, 1));
             orderId = orderIds.isEmpty() ? null : orderIds.get(0);
        }

        ReviewEntity review = new ReviewEntity();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setOrderId(orderId);
        review.setRating(rating); // Can be null for replies
        review.setComment(comment);
        review.setImages(images);
        review.setParentId(parentId);
        review.setStatus(ReviewStatus.APPROVED); // Auto approve for now

        ReviewEntity saved = reviewRepo.save(review);
        if (parentId == null && rating != null) {
            updateProductRating(productId);
        }
        return saved;
    }

    @Transactional
    public void toggleLike(Long userId, Long reviewId) {
        ReviewEntity review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("Review not found"));
        
        if (review.getLikedUserIds().contains(userId)) {
            review.removeLike(userId);
        } else {
            review.addLike(userId);
        }
        reviewRepo.save(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewEntity> listReviews(Long productId, Integer rating) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw ApiException.badRequest("Rating must be between 1 and 5");
        }
        if (rating != null) {
            return reviewRepo.findByProductIdAndStatusAndRating(productId, ReviewStatus.APPROVED, rating);
        }
        return reviewRepo.findByProductIdAndStatus(productId, ReviewStatus.APPROVED);
    }

    @Transactional
    public ReviewEntity updateReview(Long userId, Long reviewId, int rating, String comment, List<String> images) {
        if (rating < 1 || rating > 5) throw ApiException.badRequest("Rating must be between 1 and 5");

        ReviewEntity review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw ApiException.forbidden("Cannot update another user's review");
        }

        review.setRating(rating);
        review.setComment(comment);
        review.setImages(images);
        review.setStatus(ReviewStatus.PENDING);

        ReviewEntity saved = reviewRepo.save(review);
        updateProductRating(review.getProductId());
        return saved;
    }

    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        ReviewEntity review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("Review not found"));
        if (!review.getUserId().equals(userId)) {
            throw ApiException.forbidden("Cannot delete another user's review");
        }
        Long productId = review.getProductId();
        reviewRepo.delete(review);
        updateProductRating(productId);
    }

    @Transactional(readOnly = true)
    public List<ReviewEntity> adminListByStatus(ReviewStatus status) {
        return reviewRepo.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<ReviewEntity> adminSearch(ReviewStatus status,
                                          Long productId,
                                          Long userId,
                                          Integer rating,
                                          Pageable pageable) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw ApiException.badRequest("Rating must be between 1 and 5");
        }

        Specification<ReviewEntity> spec = (root, query, cb) -> {
            var predicates = cb.conjunction();
            if (status != null) {
                predicates.getExpressions().add(cb.equal(root.get("status"), status));
            }
            if (productId != null) {
                predicates.getExpressions().add(cb.equal(root.get("productId"), productId));
            }
            if (userId != null) {
                predicates.getExpressions().add(cb.equal(root.get("userId"), userId));
            }
            if (rating != null) {
                predicates.getExpressions().add(cb.equal(root.get("rating"), rating));
            }
            return predicates;
        };

        return reviewRepo.findAll(spec, pageable);
    }

    @Transactional
    public ReviewEntity adminApprove(Long reviewId) {
        ReviewEntity review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("Review not found"));
        review.setStatus(ReviewStatus.APPROVED);
        ReviewEntity saved = reviewRepo.save(review);
        updateProductRating(review.getProductId());
        return saved;
    }

    @Transactional
    public ReviewEntity adminReject(Long reviewId) {
        ReviewEntity review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> ApiException.notFound("Review not found"));
        review.setStatus(ReviewStatus.REJECTED);
        ReviewEntity saved = reviewRepo.save(review);
        updateProductRating(review.getProductId());
        return saved;
    }

    @Transactional
    public void updateProductRating(Long productId) {
        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        Double avg = reviewRepo.calculateAverageRating(productId);
        long count = reviewRepo.countByProductIdAndStatusAndParentIdIsNull(productId, ReviewStatus.APPROVED);
        double avgValue = avg == null ? 0.0 : avg;

        product.setAverageRating(BigDecimal.valueOf(avgValue).setScale(1, RoundingMode.HALF_UP));
        product.setReviewCount((int) count);
        productRepo.save(product);
    }

    @Transactional(readOnly = true)
    public java.util.Map<Integer, Long> getRatingDistribution(Long productId) {
        List<Object[]> results = reviewRepo.countByRating(productId);
        java.util.Map<Integer, Long> distribution = new java.util.HashMap<>();
        
        // Initialize all ratings to 0
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        
        // Fill in actual counts
        for (Object[] result : results) {
            Integer rating = ((Number) result[0]).intValue();
            Long count = ((Number) result[1]).longValue();
            distribution.put(rating, count);
        }
        
        return distribution;
    }
}
