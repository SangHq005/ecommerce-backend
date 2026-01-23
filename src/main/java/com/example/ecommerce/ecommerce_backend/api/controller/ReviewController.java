package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.dto.review.CreateReviewRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.review.ReviewResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.review.UpdateReviewRequest;
import com.example.ecommerce.ecommerce_backend.api.exception.BusinessException;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ErrorCode;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.ReviewService;
import com.example.ecommerce.ecommerce_backend.domain.model.RoleCode;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ReviewEntity;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Reviews", description = "Product reviews management")
public class ReviewController {

    private final ReviewService reviewService;
    private final com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository userRepo;

    public ReviewController(ReviewService reviewService, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository userRepo) {
        this.reviewService = reviewService;
        this.userRepo = userRepo;
    }

    private Long currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User not authenticated");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid User ID in token");
        }
    }

    @PostMapping("/products/{id}/reviews")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Create review", description = "Submit a review for a product")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable("id") Long productId,
            @RequestBody CreateReviewRequest request
    ) {
        ReviewEntity review = reviewService.createReview(
                currentUserId(),
                productId,
                request.rating(),
                request.comment(),
                request.images(),
                request.parentId()
        );
        
        com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity user = userRepo.findById(review.getUserId()).orElse(null);
        return ResponseHelper.created(toResponse(review, user), "Review submitted successfully");
    }

    @GetMapping("/products/{id}/reviews")
    @Operation(summary = "List reviews", description = "Get all reviews for a product")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> listReviews(
            @PathVariable("id") Long productId,
            @RequestParam(required = false) Integer rating
    ) {
        List<ReviewEntity> allReviews = reviewService.listReviews(productId, null);
        
        // Fetch users
        java.util.Set<Long> userIds = allReviews.stream().map(ReviewEntity::getUserId).collect(java.util.stream.Collectors.toSet());
        java.util.Map<Long, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity> userMap = userRepo.findAllById(userIds).stream()
                .collect(java.util.stream.Collectors.toMap(com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity::getId, java.util.function.Function.identity()));

        List<ReviewEntity> filteredRoots = allReviews.stream()
                .filter(r -> r.getParentId() == null)
                .filter(r -> rating == null || (r.getRating() != null && r.getRating().equals(rating)))
                .toList();

        java.util.Map<Long, List<ReviewEntity>> repliesMap = allReviews.stream()
                .filter(r -> r.getParentId() != null)
                .collect(java.util.stream.Collectors.groupingBy(ReviewEntity::getParentId));

        List<ReviewResponse> response = filteredRoots.stream()
                .map(root -> toResponseWithReplies(root, repliesMap, userMap))
                .toList();

        return ResponseHelper.ok(response);
    }

    @GetMapping("/products/{id}/reviews/rating-distribution")
    @Operation(summary = "Get rating distribution", description = "Get count of reviews for each rating (1-5 stars)")
    public ResponseEntity<ApiResponse<java.util.Map<Integer, Long>>> getRatingDistribution(
            @PathVariable("id") Long productId
    ) {
        java.util.Map<Integer, Long> distribution = reviewService.getRatingDistribution(productId);
        return ResponseHelper.ok(distribution);
    }
    


    @PostMapping("/reviews/{id}/like")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Toggle like", description = "Toggle like for a review")
    public ResponseEntity<ApiResponse<Void>> toggleLike(@PathVariable("id") Long reviewId) {
        reviewService.toggleLike(currentUserId(), reviewId);
        return ResponseHelper.ok(null, "Like toggled successfully");
    }

    @PutMapping("/reviews/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Update review", description = "Update your own review")
    public ResponseEntity<ApiResponse<ReviewResponse>> updateReview(
            @PathVariable("id") Long reviewId,
            @Valid @RequestBody UpdateReviewRequest request
    ) {
        ReviewEntity review = reviewService.updateReview(
                currentUserId(),
                reviewId,
                request.rating(),
                request.comment(),
                request.images()
        );
        return ResponseHelper.ok(toResponse(review, null), "Review updated successfully");
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Delete review", description = "Delete your own review")
    public ResponseEntity<ApiResponse<Void>> deleteReview(@PathVariable("id") Long reviewId) {
        reviewService.deleteReview(currentUserId(), reviewId);
        return ResponseHelper.ok(null, "Review deleted successfully");
    }

    private Long getAuthUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private ReviewResponse toResponseWithReplies(ReviewEntity review, java.util.Map<Long, List<ReviewEntity>> repliesMap, java.util.Map<Long, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity> userMap) {
        List<ReviewEntity> directReplies = repliesMap.getOrDefault(review.getId(), java.util.Collections.emptyList());
        List<ReviewResponse> replyResponses = directReplies.stream()
                .map(reply -> toResponseWithReplies(reply, repliesMap, userMap))
                .toList();
        
        Long currentUserId = getAuthUserId();
        boolean isLiked = currentUserId != null && review.getLikedUserIds().contains(currentUserId);
        
        com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity user = userMap.get(review.getUserId());
        String userName = user != null ? user.getFullName() : "Unknown";
        String userAvatar = null; 
        
        boolean isAdmin = user != null && user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(RoleCode.ADMIN.name()));

        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                userName,
                userAvatar,
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getImages(),
                review.getStatus(),
                review.getHelpfulCount(),
                replyResponses,
                isLiked,
                isAdmin,
                review.getCreatedAt()
        );
    }

    private ReviewResponse toResponse(ReviewEntity review, com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity user) {
        Long currentUserId = getAuthUserId();
        boolean isLiked = currentUserId != null && review.getLikedUserIds().contains(currentUserId);
        
        String userName = user != null ? user.getFullName() : "Unknown";
        String userAvatar = null;
        
        boolean isAdmin = user != null && user.getRoles().stream()
                .anyMatch(role -> role.getCode().equals(RoleCode.ADMIN.name()));

        return new ReviewResponse(
                review.getId(),
                review.getProductId(),
                review.getUserId(),
                userName,
                userAvatar,
                review.getOrderId(),
                review.getRating(),
                review.getComment(),
                review.getImages(),
                review.getStatus(),
                review.getHelpfulCount(),
                java.util.Collections.emptyList(),
                isLiked,
                isAdmin,
                review.getCreatedAt()
        );
    }
}
