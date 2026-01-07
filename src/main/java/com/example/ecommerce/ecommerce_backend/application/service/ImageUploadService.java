package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.upload.UploadResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageUploadService {

    private final CloudStorageService cloudStorageService;

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Upload product image
     */
    public UploadResponse uploadProductImage(MultipartFile file) {
        log.info("Uploading product image: {}", file.getOriginalFilename());

        validateImageFile(file);

        String fileUrl = cloudStorageService.uploadFile(file, "products");

        return new UploadResponse(
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * Upload user avatar
     */
    public UploadResponse uploadUserAvatar(MultipartFile file) {
        log.info("Uploading user avatar: {}", file.getOriginalFilename());

        validateImageFile(file);

        String fileUrl = cloudStorageService.uploadFile(file, "avatars");

        return new UploadResponse(
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * Upload shop logo
     */
    public UploadResponse uploadShopLogo(MultipartFile file) {
        log.info("Uploading shop logo: {}", file.getOriginalFilename());

        validateImageFile(file);

        String fileUrl = cloudStorageService.uploadFile(file, "shops");

        return new UploadResponse(
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * Upload category image
     */
    public UploadResponse uploadCategoryImage(MultipartFile file) {
        log.info("Uploading category image: {}", file.getOriginalFilename());

        validateImageFile(file);

        String fileUrl = cloudStorageService.uploadFile(file, "categories");

        return new UploadResponse(
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

    /**
     * Delete image
     */
    public void deleteImage(String fileUrl) {
        log.info("Deleting image: {}", fileUrl);
        cloudStorageService.deleteFile(fileUrl);
    }

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ApiException.badRequest("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw ApiException.badRequest("File size exceeds maximum allowed size (10MB)");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw ApiException.badRequest("Only image files (JPEG, PNG, GIF, WebP) are allowed");
        }
    }
}
