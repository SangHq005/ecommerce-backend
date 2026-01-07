package com.example.ecommerce.ecommerce_backend.api.dto.upload;

public record UploadResponse(
        String fileName,
        String fileUrl,
        String fileType,
        long fileSize
) {
}
