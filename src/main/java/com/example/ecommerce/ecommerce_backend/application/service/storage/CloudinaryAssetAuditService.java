package com.example.ecommerce.ecommerce_backend.application.service.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.config.CloudStorageConfig;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.BrandJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductImageJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserProfileJpaRepository;

@Service
public class CloudinaryAssetAuditService {

    private static final String CLOUDINARY_HOST = "res.cloudinary.com";

    private final CloudStorageConfig config;
    private final ProductJpaRepository productRepo;
    private final ProductImageJpaRepository productImageRepo;
    private final SkuJpaRepository skuRepo;
    private final SellerShopJpaRepository shopRepo;
    private final UserProfileJpaRepository userProfileRepo;
    private final BrandJpaRepository brandRepo;

    public CloudinaryAssetAuditService(
            CloudStorageConfig config,
            ProductJpaRepository productRepo,
            ProductImageJpaRepository productImageRepo,
            SkuJpaRepository skuRepo,
            SellerShopJpaRepository shopRepo,
            UserProfileJpaRepository userProfileRepo,
            BrandJpaRepository brandRepo
    ) {
        this.config = config;
        this.productRepo = productRepo;
        this.productImageRepo = productImageRepo;
        this.skuRepo = skuRepo;
        this.shopRepo = shopRepo;
        this.userProfileRepo = userProfileRepo;
        this.brandRepo = brandRepo;
    }

    public record CloudinaryAuditReport(
            String storagePrefix,
            int totalCloudinaryUrls,
            int organizedUrls,
            int legacyUrls,
            int nonCloudinaryUrls,
            List<String> sampleLegacyUrls
    ) {}

    @Transactional(readOnly = true)
    public CloudinaryAuditReport audit() {
        String prefix = config.getPrefix() != null && !config.getPrefix().isBlank()
                ? config.getPrefix()
                : "shopmart";
        String organizedMarker = "/" + prefix + "/";

        Set<String> cloudinaryUrls = new LinkedHashSet<>();
        Set<String> allUrls = new LinkedHashSet<>();

        productRepo.findAll().stream()
                .map(p -> p.getMainImageUrl())
                .forEach(url -> collectUrl(url, allUrls, cloudinaryUrls));
        productImageRepo.findAll().stream()
                .map(i -> i.getImageUrl())
                .forEach(url -> collectUrl(url, allUrls, cloudinaryUrls));
        skuRepo.findAll().stream()
                .map(s -> s.getImageUrl())
                .forEach(url -> collectUrl(url, allUrls, cloudinaryUrls));
        shopRepo.findAll().forEach(shop -> {
            collectUrl(shop.getLogoUrl(), allUrls, cloudinaryUrls);
            collectUrl(shop.getBannerUrl(), allUrls, cloudinaryUrls);
        });
        userProfileRepo.findAll().stream()
                .map(p -> p.getAvatarUrl())
                .forEach(url -> collectUrl(url, allUrls, cloudinaryUrls));
        brandRepo.findAll().stream()
                .map(b -> b.getLogoUrl())
                .forEach(url -> collectUrl(url, allUrls, cloudinaryUrls));

        int organized = 0;
        int legacy = 0;
        List<String> sampleLegacy = new ArrayList<>();

        for (String url : cloudinaryUrls) {
            if (url.contains(organizedMarker)) {
                organized++;
            } else {
                legacy++;
                if (sampleLegacy.size() < 10) {
                    sampleLegacy.add(url);
                }
            }
        }

        return new CloudinaryAuditReport(
                prefix,
                cloudinaryUrls.size(),
                organized,
                legacy,
                allUrls.size() - cloudinaryUrls.size(),
                sampleLegacy
        );
    }

    private void collectUrl(String url, Set<String> allUrls, Set<String> cloudinaryUrls) {
        if (url == null || url.isBlank()) {
            return;
        }
        allUrls.add(url);
        if (url.contains(CLOUDINARY_HOST)) {
            cloudinaryUrls.add(url);
        }
    }
}
