package com.example.ecommerce.ecommerce_backend.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.example.ecommerce.ecommerce_backend.api.response.ResponseHelper;
import com.example.ecommerce.ecommerce_backend.application.service.storage.CloudinaryAssetAuditService;
import com.example.ecommerce.ecommerce_backend.application.service.storage.CloudinaryAssetAuditService.CloudinaryAuditReport;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerLocalImageMigrationService;
import com.example.ecommerce.ecommerce_backend.application.service.seller.SellerLocalImageMigrationService.MigrationResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/maintenance")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Maintenance", description = "Admin maintenance operations")
public class AdminMaintenanceController {

    private final SellerLocalImageMigrationService migrationService;
    private final CloudinaryAssetAuditService auditService;

    public AdminMaintenanceController(
            SellerLocalImageMigrationService migrationService,
            CloudinaryAssetAuditService auditService
    ) {
        this.migrationService = migrationService;
        this.auditService = auditService;
    }

    @GetMapping("/cloudinary-audit")
    @Operation(summary = "Cloudinary URL audit", description = "Report Cloudinary URLs in DB: organized vs legacy (root) paths")
    public ResponseEntity<ApiResponse<CloudinaryAuditReport>> cloudinaryAudit() {
        CloudinaryAuditReport report = auditService.audit();
        return ResponseHelper.ok(report, "Cloudinary audit completed");
    }

    @PostMapping("/migrate-local-seller-images")
    @Operation(summary = "Migrate local seller images", description = "Upload /files/... seller images from disk to Cloudinary and update DB URLs")
    public ResponseEntity<ApiResponse<MigrationResult>> migrateLocalSellerImages() {
        MigrationResult result = migrationService.migrateAll();
        return ResponseHelper.ok(result, "Seller local image migration completed");
    }
}
