package com.example.ecommerce.ecommerce_backend.api.dto.search;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements
) {}
