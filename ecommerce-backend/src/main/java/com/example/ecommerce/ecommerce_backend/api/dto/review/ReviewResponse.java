package com.example.ecommerce.ecommerce_backend.api.dto.review;

import com.example.ecommerce.ecommerce_backend.domain.review.ReviewStatus;

import java.time.Instant;
import java.util.List;

public record ReviewResponse(
        Long id,
        Long productId,
        Long userId,
        Long orderId,
        Integer rating,
        String comment,
        List<String> images,
        ReviewStatus status,
        Instant createdAt
) {}
