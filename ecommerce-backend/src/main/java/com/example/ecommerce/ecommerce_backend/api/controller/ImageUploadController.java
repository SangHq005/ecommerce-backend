package com.example.ecommerce.ecommerce_backend.api.controller;

import com.example.ecommerce.ecommerce_backend.api.dto.MessageResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.upload.UploadResponse;
import com.example.ecommerce.ecommerce_backend.application.service.ImageUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
public class ImageUploadController {

    private final ImageUploadService uploadService;

    /**
     * Upload product image
     */
    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received product image upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadProductImage(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload multiple product images
     */
    @PostMapping(value = "/product/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<java.util.List<UploadResponse>> uploadProductImages(
            @RequestParam("files") MultipartFile[] files
    ) {
        log.info("Received multiple product images upload request: {} files", files.length);

        java.util.List<UploadResponse> responses = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            UploadResponse response = uploadService.uploadProductImage(file);
            responses.add(response);
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * Upload user avatar
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadUserAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received avatar upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadUserAvatar(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload shop logo
     */
    @PostMapping(value = "/shop/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadShopLogo(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received shop logo upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadShopLogo(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload category image
     */
    @PostMapping(value = "/category", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> uploadCategoryImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received category image upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadCategoryImage(file);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete image
     */
    @DeleteMapping
    public ResponseEntity<MessageResponse> deleteImage(
            @RequestParam("fileUrl") String fileUrl
    ) {
        log.info("Received delete image request: {}", fileUrl);
        uploadService.deleteImage(fileUrl);
        return ResponseEntity.ok(new MessageResponse("Image deleted successfully"));
    }
}
