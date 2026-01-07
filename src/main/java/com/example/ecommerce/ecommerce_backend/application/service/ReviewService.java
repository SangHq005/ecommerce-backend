package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ReviewJpaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ReviewEntity createReview(Long userId, Long productId, int rating, String comment, List<String> images) {
        if (rating < 1 || rating > 5) throw ApiException.badRequest("Rating must be between 1 and 5");

        ProductEntity product = productRepo.findById(productId)
                .orElseThrow(() -> ApiException.notFound("Product not found"));

        if (reviewRepo.findByProductIdAndUserId(productId, userId).isPresent()) {
            throw ApiException.conflict("Only one review per product is allowed");
        }

        List<String> statuses = List.of(OrderStatus.PAID.name(), OrderStatus.FULFILLED.name());
        List<Long> orderIds = orderItemRepo.findPurchasedOrderIds(userId, productId, statuses, PageRequest.of(0, 1));
        if (orderIds.isEmpty()) {
            throw ApiException.badRequest("User must have purchased this product");
        }

        ReviewEntity review = new ReviewEntity();
        review.setProductId(productId);
        review.setUserId(userId);
        review.setOrderId(orderIds.get(0));
        review.setRating(rating);
        review.setComment(comment);
        review.setImages(images);
        review.setStatus(ReviewStatus.PENDING);

        ReviewEntity saved = reviewRepo.save(review);
        updateProductRating(product.getId());
        return saved;
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
        long count = reviewRepo.countByProductIdAndStatus(productId, ReviewStatus.APPROVED);
        double avgValue = avg == null ? 0.0 : avg;

        product.setAverageRating(BigDecimal.valueOf(avgValue).setScale(1, RoundingMode.HALF_UP));
        product.setReviewCount((int) count);
        productRepo.save(product);
    }
}
