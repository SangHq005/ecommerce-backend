package com.example.ecommerce.ecommerce_backend.application.service.storage;

import com.example.ecommerce.ecommerce_backend.api.dto.upload.UploadResponse;
import com.example.ecommerce.ecommerce_backend.api.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class ImageUploadService {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadService.class);

    private final CloudStorageService cloudStorageService;

    public ImageUploadService(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg",
            "image/jpg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public UploadResponse uploadProductImage(MultipartFile file) {
        log.info("Uploading product image: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.PRODUCTS);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadUserAvatar(MultipartFile file) {
        log.info("Uploading user avatar: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.AVATARS);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadShopLogo(MultipartFile file) {
        log.info("Uploading shop logo: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.SHOP_LOGOS);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadShopBanner(MultipartFile file) {
        log.info("Uploading shop banner: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.SHOP_BANNERS);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadCategoryImage(MultipartFile file) {
        log.info("Uploading category image: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.CATEGORIES);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadReviewImage(MultipartFile file) {
        log.info("Uploading review image: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.REVIEWS);
        return toResponse(file, fileUrl);
    }

    public UploadResponse uploadSellerDocument(MultipartFile file) {
        log.info("Uploading seller document: {}", file.getOriginalFilename());
        validateImageFile(file);
        String fileUrl = cloudStorageService.uploadFile(file, CloudinaryFolders.SELLER_DOCUMENTS);
        return toResponse(file, fileUrl);
    }

    public void deleteImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank() || fileUrl.startsWith("/files/")) {
            return;
        }
        log.info("Deleting image: {}", fileUrl);
        try {
            cloudStorageService.deleteFile(fileUrl);
        } catch (Exception e) {
            log.warn("Failed to delete image from cloud storage: {}", fileUrl, e);
        }
    }

    private UploadResponse toResponse(MultipartFile file, String fileUrl) {
        return new UploadResponse(
                file.getOriginalFilename(),
                fileUrl,
                file.getContentType(),
                file.getSize()
        );
    }

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
