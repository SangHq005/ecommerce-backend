package com.example.ecommerce.ecommerce_backend.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.ecommerce.ecommerce_backend.api.dto.upload.UploadResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.storage.ImageUploadService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "Image Upload", description = "Image upload endpoints")
public class ImageUploadController {

    private static final Logger log = LoggerFactory.getLogger(ImageUploadController.class);

    private final ImageUploadService uploadService;

    public ImageUploadController(ImageUploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product image", description = "Upload a product image")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadProductImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received product image upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadProductImage(file);
        return ResponseHelper.created(response, "Image uploaded successfully");
    }

    @PostMapping(value = "/product/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple images", description = "Upload multiple product images")
    public ResponseEntity<ApiResponse<List<UploadResponse>>> uploadProductImages(
            @RequestParam("files") MultipartFile[] files
    ) {
        log.info("Received multiple product images upload request: {} files", files.length);
        List<UploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            UploadResponse response = uploadService.uploadProductImage(file);
            responses.add(response);
        }
        return ResponseHelper.created(responses, files.length + " images uploaded successfully");
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload avatar", description = "Upload user avatar")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadUserAvatar(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received avatar upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadUserAvatar(file);
        return ResponseHelper.created(response, "Avatar uploaded successfully");
    }

    @PostMapping(value = "/shop/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload shop logo", description = "Upload shop logo image")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadShopLogo(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received shop logo upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadShopLogo(file);
        return ResponseHelper.created(response, "Logo uploaded successfully");
    }

    @PostMapping(value = "/category", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload category image", description = "Upload category image")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadCategoryImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received category image upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadCategoryImage(file);
        return ResponseHelper.created(response, "Category image uploaded successfully");
    }

    @PostMapping(value = "/review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload review image", description = "Upload image for product review")
    public ResponseEntity<ApiResponse<UploadResponse>> uploadReviewImage(
            @RequestParam("file") MultipartFile file
    ) {
        log.info("Received review image upload request: {}", file.getOriginalFilename());
        UploadResponse response = uploadService.uploadReviewImage(file);
        return ResponseHelper.created(response, "Review image uploaded successfully");
    }

    @PostMapping(value = "/review/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload multiple review images", description = "Upload multiple images for product review")
    public ResponseEntity<ApiResponse<List<UploadResponse>>> uploadReviewImages(
            @RequestParam("files") MultipartFile[] files
    ) {
        log.info("Received multiple review images upload request: {} files", files.length);
        List<UploadResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            UploadResponse response = uploadService.uploadReviewImage(file);
            responses.add(response);
        }
        return ResponseHelper.created(responses, files.length + " review images uploaded successfully");
    }

    @DeleteMapping
    @Operation(summary = "Delete image", description = "Delete an uploaded image")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @RequestParam("fileUrl") String fileUrl
    ) {
        log.info("Received delete image request: {}", fileUrl);
        uploadService.deleteImage(fileUrl);
        return ResponseHelper.ok(null, "Image deleted successfully");
    }
}
