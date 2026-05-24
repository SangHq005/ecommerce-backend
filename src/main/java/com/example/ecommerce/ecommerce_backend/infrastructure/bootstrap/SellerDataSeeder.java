package com.example.ecommerce.ecommerce_backend.infrastructure.bootstrap;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.OrderItemEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.BrandJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CategoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderItemJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;

/**
 * Seed seller data: products and orders for testing seller management features
 */
@Component
public class SellerDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SellerDataSeeder.class);

    private final UserJpaRepository userRepo;
    private final SellerShopJpaRepository shopRepo;
    private final CategoryJpaRepository categoryRepo;
    private final BrandJpaRepository brandRepo;
    private final ProductJpaRepository productRepo;
    private final SkuJpaRepository skuRepo;
    private final OrderJpaRepository orderRepo;
    private final OrderItemJpaRepository orderItemRepo;
    private final UserAddressJpaRepository addressRepo;

    public SellerDataSeeder(
            UserJpaRepository userRepo,
            SellerShopJpaRepository shopRepo,
            CategoryJpaRepository categoryRepo,
            BrandJpaRepository brandRepo,
            ProductJpaRepository productRepo,
            SkuJpaRepository skuRepo,
            OrderJpaRepository orderRepo,
            OrderItemJpaRepository orderItemRepo,
            UserAddressJpaRepository addressRepo
    ) {
        this.userRepo = userRepo;
        this.shopRepo = shopRepo;
        this.categoryRepo = categoryRepo;
        this.brandRepo = brandRepo;
        this.productRepo = productRepo;
        this.skuRepo = skuRepo;
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.addressRepo = addressRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String email = "quocsang05.uit@gmail.com";
        seedSellerData(email);
    }

    private void seedSellerData(String email) {
        try {
            // Find user and shop
            Optional<UserEntity> userOpt = userRepo.findByEmail(email);
            if (userOpt.isEmpty()) {
                log.warn("User with email {} not found. Skipping seller data seed.", email);
                return;
            }

            UserEntity user = userOpt.get();
            Long userId = user.getId();
            log.info("Found user: {} (ID: {})", email, userId);

            Optional<SellerShopEntity> shopOpt = shopRepo.findBySellerUserId(userId);
            if (shopOpt.isEmpty()) {
                log.warn("Shop not found for user {}. Please create shop first.", email);
                return;
            }

            SellerShopEntity shop = shopOpt.get();
            Long shopId = shop.getId();
            log.info("Found shop: {} (ID: {})", shop.getShopName(), shopId);

            // Get or create category and brand
            CategoryEntity category = getOrCreateCategory("Điện thoại & Phụ kiện", "dien-thoai-phu-kien");
            BrandEntity brand = getOrCreateBrand("Samsung", "samsung");

            // Seed products
            List<ProductEntity> products = seedProducts(shopId, userId, category.getId(), brand.getId());
            log.info("Created {} products", products.size());

            // Seed orders
            seedOrders(shopId, userId, products);
            log.info("Seller data seeding completed for user {}", email);

        } catch (RuntimeException | Error e) {
            log.error("Error seeding seller data for user {}: {}", email, e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Error seeding seller data for user {}: {}", email, e.getMessage(), e);
        }
    }

    private CategoryEntity getOrCreateCategory(String name, String slug) {
        // Try to find existing category by name
        List<CategoryEntity> allCategories = categoryRepo.findAll();
        Optional<CategoryEntity> existing = allCategories.stream()
                .filter(c -> c.getSlug().equals(slug) || c.getName().equals(name))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }

        CategoryEntity category = new CategoryEntity();
        category.setName(name);
        category.setSlug(slug);
        category.setPath("/" + slug);
        category.setActive(true);
        category.setSortOrder(1);
        category = categoryRepo.save(category);
        log.info("Created category: {}", name);
        return category;
    }

    private BrandEntity getOrCreateBrand(String name, String slug) {
        // Try to find existing brand by name
        List<BrandEntity> allBrands = brandRepo.findAll();
        Optional<BrandEntity> existing = allBrands.stream()
                .filter(b -> b.getSlug().equals(slug) || b.getName().equals(name))
                .findFirst();
        if (existing.isPresent()) {
            return existing.get();
        }

        BrandEntity brand = new BrandEntity();
        brand.setName(name);
        brand.setSlug(slug);
        brand.setActive(true);
        brand = brandRepo.save(brand);
        log.info("Created brand: {}", name);
        return brand;
    }

    private List<ProductEntity> seedProducts(Long shopId, Long sellerUserId, Long categoryId, Long brandId) {
        List<ProductEntity> products = new ArrayList<>();

        // Sample products
        record ProductData(String name, String slug, String description, Long price, Long originalPrice, Integer stock) {}
        ProductData[] productDataArray = {
            new ProductData("Samsung Galaxy S24 Ultra", "samsung-galaxy-s24-ultra", 
                "Điện thoại Samsung Galaxy S24 Ultra 256GB - Flagship mới nhất với camera 200MP, chip Snapdragon 8 Gen 3", 
                24990000L, 26990000L, 50),
            new ProductData("Samsung Galaxy A55", "samsung-galaxy-a55", 
                "Điện thoại Samsung Galaxy A55 128GB - Hiệu năng mạnh mẽ, camera ấn tượng", 
                8990000L, 9990000L, 100),
            new ProductData("Samsung Galaxy Watch 6", "samsung-galaxy-watch-6", 
                "Đồng hồ thông minh Samsung Galaxy Watch 6 - Theo dõi sức khỏe 24/7", 
                5990000L, 6990000L, 30),
            new ProductData("Tai nghe Samsung Galaxy Buds2 Pro", "samsung-galaxy-buds2-pro", 
                "Tai nghe không dây Samsung Galaxy Buds2 Pro - Chất lượng âm thanh cao cấp", 
                3990000L, 4990000L, 75),
            new ProductData("Sạc nhanh Samsung 25W", "sac-nhanh-samsung-25w", 
                "Củ sạc nhanh Samsung 25W chính hãng - Sạc siêu tốc cho điện thoại", 
                490000L, 690000L, 200)
        };

        for (ProductData data : productDataArray) {
            String name = data.name();
            String baseSlug = data.slug();
            String description = data.description();
            Long price = data.price();
            Long originalPrice = data.originalPrice();
            Integer stock = data.stock();

            // Build full slug with shopId prefix
            String fullSlug = shopId + "-" + baseSlug;
            
            // Check if product already exists by slug (with shopId prefix) or by name and shopId
            Optional<ProductEntity> existing = productRepo.findBySlug(fullSlug);
            if (existing.isEmpty()) {
                // Also check by name and shopId to avoid duplicates
                List<ProductEntity> shopProducts = productRepo.findByShopIdAndStatus(shopId, "ACTIVE");
                existing = shopProducts.stream()
                        .filter(p -> p.getName().equals(name))
                        .findFirst();
            }
            
            if (existing.isPresent()) {
                log.info("Product '{}' already exists (ID: {}), skipping...", name, existing.get().getId());
                products.add(existing.get());
                continue;
            }

            ProductEntity product = new ProductEntity();
            product.setShopId(shopId);
            product.setSellerUserId(sellerUserId);
            product.setCategoryId(categoryId);
            product.setBrandId(brandId);
            product.setName(name);
            product.setSlug(fullSlug); // Ensure unique slug per shop
            product.setDescription(description);
            product.setStatus("ACTIVE");
            product.setPrice(price);
            product.setOriginalPrice(originalPrice);
            product.setStockQuantity(stock);
            product.setSku("SKU-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000));
            product.setCurrency("VND");
            product.setAverageRating(BigDecimal.valueOf(4.0 + Math.random() * 1.0)); // 4.0 - 5.0
            product.setReviewCount((int)(Math.random() * 100) + 10);
            product.setSoldCount((int)(Math.random() * 50));
            product.setPublishedAt(Instant.now());
            product.setIsFeatured(false);
            product.setWeightGrams(200 + (int)(Math.random() * 500));
            product.setShippingFeeType("STANDARD");

            product = productRepo.save(product);
            products.add(product);
            log.info("Created product: {} (ID: {})", name, product.getId());

            // Create default SKU for product
            SkuEntity sku = new SkuEntity();
            sku.setProductId(product.getId());
            sku.setSkuCode("DEFAULT-" + product.getId());
            sku.setOptionSignature("default");
            sku.setOptionSignatureHash(hashString("default"));
            sku.setPrice(price);
            sku.setCompareAtPrice(originalPrice);
            sku.setStockOnHand(stock);
            sku.setActive(true);
            sku.setReservedStock(0);
            sku = skuRepo.save(sku);
            log.info("Created SKU for product {}: {}", name, sku.getId());
        }

        return products;
    }

    private void seedOrders(Long shopId, Long sellerUserId, List<ProductEntity> products) {
        if (products.isEmpty()) {
            log.warn("No products available to create orders");
            return;
        }

        // Get a buyer user (create a test buyer if needed)
        UserEntity buyer = getOrCreateBuyer();
        Long buyerId = buyer.getId();
        
        // Get or create address for buyer
        UserAddressEntity buyerAddress = getOrCreateBuyerAddress(buyerId);

        // Create orders with different statuses
        // More PROCESSING orders for batch shipping testing
        String[] orderStatuses = {"PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "DELIVERED", "COMPLETED"};
        int[] orderCounts = {2, 2, 8, 3, 2, 1}; // More PROCESSING orders (8) for batch shipping

        int orderIndex = 0;
        for (int statusIndex = 0; statusIndex < orderStatuses.length; statusIndex++) {
            String status = orderStatuses[statusIndex];
            int count = orderCounts[statusIndex];

            for (int i = 0; i < count; i++) {
                if (orderIndex >= products.size()) {
                    orderIndex = 0; // Cycle through products
                }

                ProductEntity product = products.get(orderIndex % products.size());
                List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(product.getId());
                if (skus.isEmpty()) {
                    log.warn("No SKU found for product {}, skipping order creation", product.getName());
                    orderIndex++;
                    continue;
                }

                SkuEntity sku = skus.get(0);
                int quantity = (int)(Math.random() * 3) + 1; // 1-3 items
                long unitPrice = sku.getPrice();
                long totalPrice = unitPrice * quantity;
                long shippingFee = 30000L;
                long totalAmount = totalPrice + shippingFee;

                // Generate unique order code
                String orderCode = "ORD" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);

                OrderEntity order = new OrderEntity();
                order.setOrderCode(orderCode);
                order.setUserId(buyerId);
                order.setShopId(shopId);
                order.setStatus(status);
                order.setTotalAmount(totalAmount);
                order.setCurrency("VND");
                order.setPaymentMethod("COD");
                order.setNote("Đơn hàng test - " + status);
                order.setDiscountAmount(0L);
                order.setShippingFee(shippingFee);
                order.setBuyerConfirmed(false);
                order.setAddressId(buyerAddress.getId()); // Set address for order

                // Set timestamps based on status
                // Note: createdAt is set automatically by @PrePersist, so we save first then update
                order = orderRepo.save(order);
                
                LocalDateTime now = LocalDateTime.now();
                switch (status) {
                    case "CONFIRMED" -> {
                        // Order already saved with current timestamp
                    }
                    case "PROCESSING" -> {
                        // Order already saved with current timestamp
                    }
                    case "SHIPPED" -> {
                        order.setShippedAt(now.minusDays((int)(Math.random() * 3)));
                        order.setTrackingNumber("TRACK" + System.currentTimeMillis());
                        order.setShippingProvider("GHN");
                        orderRepo.save(order);
                    }
                    case "DELIVERED" -> {
                        order.setShippedAt(now.minusDays(2));
                        order.setDeliveredAt(now.minusDays(1));
                        order.setTrackingNumber("TRACK" + System.currentTimeMillis());
                        order.setShippingProvider("GHN");
                        orderRepo.save(order);
                    }
                    case "COMPLETED" -> {
                        order.setShippedAt(now.minusDays(7));
                        order.setDeliveredAt(now.minusDays(6));
                        order.setCompletedAt(now.minusDays(5));
                        order.setBuyerConfirmed(true);
                        order.setBuyerConfirmedAt(now.minusDays(5));
                        order.setTrackingNumber("TRACK" + System.currentTimeMillis());
                        order.setShippingProvider("GHN");
                        orderRepo.save(order);
                    }
                }

                // Order already saved above in switch statement
                log.info("Created order: {} (Status: {})", orderCode, status);

                // Create order item
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrderId(order.getId());
                orderItem.setProductId(product.getId());
                orderItem.setSkuId(sku.getId());
                orderItem.setQuantity(quantity);
                orderItem.setUnitPrice(unitPrice);
                orderItem.setTotalPrice(totalPrice);
                orderItemRepo.save(orderItem);
                log.info("Created order item for order {}", orderCode);

                orderIndex++;
            }
        }

        log.info("Created {} orders with various statuses", Arrays.stream(orderCounts).sum());
    }

    private UserEntity getOrCreateBuyer() {
        // Try to find an existing buyer (non-seller user)
        List<UserEntity> allUsers = userRepo.findAll();
        for (UserEntity u : allUsers) {
            boolean isSeller = u.getRoles().stream()
                    .anyMatch(r -> "SELLER".equals(r.getCode()));
            if (!isSeller && u.getEmail() != null && !u.getEmail().equals("quocsang05.uit@gmail.com")) {
                return u;
            }
        }

        // Create a test buyer if none found
        UserEntity buyer = new UserEntity();
        buyer.setEmail("test.buyer@example.com");
        buyer.setPhoneNumber("0900000000");
        buyer.setFullName("Test Buyer");
        buyer.setStatus("ACTIVE");
        buyer = userRepo.save(buyer);
        log.info("Created test buyer user: {}", buyer.getEmail());
        return buyer;
    }

    private UserAddressEntity getOrCreateBuyerAddress(Long userId) {
        // Try to find existing address for user
        List<UserAddressEntity> addresses = addressRepo.findByUserIdOrderByIsDefaultDescIdDesc(userId);
        if (!addresses.isEmpty()) {
            return addresses.get(0);
        }

        // Create a default address
        UserAddressEntity address = new UserAddressEntity();
        address.setUserId(userId);
        address.setReceiverName("Test Buyer");
        address.setReceiverPhone("0900000000");
        address.setLine1("123 Đường ABC");
        address.setWard("Phường 1");
        address.setDistrict("Quận 1");
        address.setProvince("TP. Hồ Chí Minh");
        address.setPostalCode("700000");
        address.setAddressType("HOME");
        address.setDefault(true);
        // createdAt and updatedAt are set automatically by @PrePersist/@PreUpdate
        address = addressRepo.save(address);
        log.info("Created address for buyer user ID: {}", userId);
        return address;
    }

    private String hashString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString().replace("-", "");
        }
    }
}
