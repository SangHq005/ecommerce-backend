package com.example.ecommerce.ecommerce_backend;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.application.service.auth.PasswordHasher;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.BrandEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.CategoryEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.ProductEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SellerShopEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.SkuEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserAddressEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.UserEntity;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.BrandJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.CategoryJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.OrderJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.ProductJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.RoleJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SellerShopJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.SkuJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.StockReservationJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserAddressJpaRepository;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Load test for concurrent checkouts
 * Tests: 100 concurrent checkout requests
 * Verifies: No deadlocks, timeouts, correct stock reservation
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class ConcurrentCheckoutLoadTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserJpaRepository userRepo;

    @Autowired
    private RoleJpaRepository roleRepo;

    @Autowired
    private ProductJpaRepository productRepo;

    @Autowired
    private SkuJpaRepository skuRepo;

    @Autowired
    private UserAddressJpaRepository addressRepo;

    @Autowired
    private OrderJpaRepository orderRepo;

    @Autowired
    private StockReservationJpaRepository resRepo;

    @Autowired
    private CategoryJpaRepository categoryRepo;

    @Autowired
    private BrandJpaRepository brandRepo;

    @Autowired
    private SellerShopJpaRepository shopRepo;

    @Autowired
    private PasswordHasher hasher;

    @Autowired
    private com.example.ecommerce.ecommerce_backend.application.service.auth.JwtService jwtService;

    private static final int CONCURRENT_REQUESTS = 100;
    private static final int INITIAL_STOCK = 200;
    private static Long testProductId;
    private static Long testSkuId;

    @Test
    @Order(1)
    @DisplayName("Setup test data")
    void setup() {
        // Clear existing data
        resRepo.deleteAll();
        orderRepo.deleteAll();
        skuRepo.deleteAll();
        productRepo.deleteAll();
        shopRepo.deleteAll();
        categoryRepo.deleteAll();
        brandRepo.deleteAll();

        // Create seller
        UserEntity seller = userRepo.findByEmail("loadtest_seller@demo.local").orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setEmail("loadtest_seller@demo.local");
            u.setFullName("Load Test Seller");
            u.setStatus("ACTIVE");
            u.setPasswordHash(hasher.hash("Password123!"));
            return u;
        });
        var sellerRole = roleRepo.findByCode("SELLER").orElseThrow();
        if (!seller.getRoles().contains(sellerRole)) {
            seller.getRoles().add(sellerRole);
        }
        seller = userRepo.save(seller);

        // Create shop
        SellerShopEntity shop = new SellerShopEntity();
        shop.setSellerUserId(seller.getId());
        shop.setShopName("Load Test Shop");
        shop.setShopSlug("loadtest-shop-" + UUID.randomUUID().toString().substring(0, 8));
        shop.setStatus("ACTIVE");
        shop = shopRepo.save(shop);

        // Create category
        CategoryEntity cat = new CategoryEntity();
        cat.setName("LoadTest Category");
        cat.setSlug("loadtest-cat-" + UUID.randomUUID().toString().substring(0, 8));
        cat.setPath("LoadTest");
        cat = categoryRepo.save(cat);

        // Create brand
        BrandEntity brand = new BrandEntity();
        brand.setName("LoadTest Brand");
        brand.setSlug("loadtest-brand-" + UUID.randomUUID().toString().substring(0, 8));
        brand = brandRepo.save(brand);

        // Create product
        ProductEntity product = new ProductEntity();
        product.setShopId(shop.getId());
        product.setSellerUserId(seller.getId());
        product.setCategoryId(cat.getId());
        product.setBrandId(brand.getId());
        product.setName("Load Test Product");
        product.setSlug("loadtest-product-" + UUID.randomUUID().toString().substring(0, 8));
        product.setDescription("Product for load testing");
        product.setStatus("ACTIVE");
        product.setPrice(100000L);
        product.setStockQuantity(INITIAL_STOCK);
        product = productRepo.save(product);
        testProductId = product.getId();

        // Create SKU with enough stock for concurrent tests
        SkuEntity sku = new SkuEntity();
        sku.setProductId(product.getId());
        sku.setSkuCode("LOADTEST-SKU-001");
        sku.setOptionSignature("Size:One");
        sku.setOptionSignatureHash(DigestUtils.sha256Hex("Size:One"));
        sku.setPrice(100000L);
        sku.setStockOnHand(INITIAL_STOCK);
        sku.setReservedStock(0);
        sku.setActive(true);
        sku = skuRepo.save(sku);
        testSkuId = sku.getId();

        System.out.println("Load test setup complete: productId=" + testProductId + ", skuId=" + testSkuId + ", stock=" + INITIAL_STOCK);
    }

    @Test
    @Order(2)
    @DisplayName("100 concurrent checkout requests - verify no deadlocks")
    void testConcurrentCheckouts() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_REQUESTS);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger conflictCount = new AtomicInteger(0);
        List<Long> responseTimes = new CopyOnWriteArrayList<>();

        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            final int userId = i + 1000; // Use unique user IDs
            
            futures.add(executor.submit(() -> {
                try {
                    // Create test user for this request
                    String email = "loadtest_user_" + userId + "@demo.local";
                    UserEntity user = userRepo.findByEmail(email).orElseGet(() -> {
                        UserEntity u = new UserEntity();
                        u.setEmail(email);
                        u.setFullName("Load Test User " + userId);
                        u.setStatus("ACTIVE");
                        u.setPasswordHash(hasher.hash("Password123!"));
                        return u;
                    });
                    var clientRole = roleRepo.findByCode("CLIENT").orElseThrow();
                    if (!user.getRoles().contains(clientRole)) {
                        user.getRoles().add(clientRole);
                    }
                    user = userRepo.save(user);

                    // Create address
                    UserAddressEntity addr = new UserAddressEntity();
                    addr.setUserId(user.getId());
                    addr.setReceiverName("Load Test " + userId);
                    addr.setReceiverPhone("0900000" + userId);
                    addr.setLine1("Test Street " + userId);
                    addr.setWard("Ward 1");
                    addr.setDistrict("District 1");
                    addr.setProvince("HCM");
                    addr.setDefault(true);
                    addr = addressRepo.save(addr);

                    // Generate JWT token
                    String jti = jwtService.newJti();
                    String token = jwtService.issueAccessToken(user.getId(), user.getEmail(), List.of("CLIENT"), jti);

                    // Create checkout request (qty=1 to allow many concurrent orders)
                    CheckoutRequest.Item item = new CheckoutRequest.Item(testProductId, testSkuId, 1);
                    CheckoutRequest req = new CheckoutRequest(
                            List.of(item),
                            addr.getId(),
                            "COD",
                            "Load test order " + userId,
                            null
                    );

                    String idemKey = UUID.randomUUID().toString();

                    long startTime = System.currentTimeMillis();
                    
                    var result = mockMvc.perform(post("/api/v1/checkout")
                            .header("Authorization", "Bearer " + token)
                            .header("Idempotency-Key", idemKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                            .andReturn();
                    
                    long elapsed = System.currentTimeMillis() - startTime;
                    responseTimes.add(elapsed);

                    int status = result.getResponse().getStatus();
                    if (status == 200) {
                        successCount.incrementAndGet();
                    } else if (status == 409) {
                        conflictCount.incrementAndGet(); // Expected for stock exhaustion
                    } else {
                        failCount.incrementAndGet();
                        System.err.println("Unexpected status " + status + ": " + result.getResponse().getContentAsString());
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.err.println("Request failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }));
        }

        // Wait for all requests to complete (timeout: 2 minutes)
        boolean completed = latch.await(2, TimeUnit.MINUTES);
        executor.shutdown();

        // Print results
        System.out.println("\n========== LOAD TEST RESULTS ==========");
        System.out.println("Total requests: " + CONCURRENT_REQUESTS);
        System.out.println("Successful: " + successCount.get());
        System.out.println("Conflicts (stock exhausted): " + conflictCount.get());
        System.out.println("Failed: " + failCount.get());
        System.out.println("Completed within timeout: " + completed);
        
        if (!responseTimes.isEmpty()) {
            long avgTime = responseTimes.stream().mapToLong(Long::longValue).sum() / responseTimes.size();
            long maxTime = responseTimes.stream().mapToLong(Long::longValue).max().orElse(0);
            long minTime = responseTimes.stream().mapToLong(Long::longValue).min().orElse(0);
            System.out.println("Response times - Avg: " + avgTime + "ms, Min: " + minTime + "ms, Max: " + maxTime + "ms");
        }

        // Verify no deadlocks occurred (all requests completed)
        assertTrue(completed, "All requests should complete within timeout (no deadlocks)");
        
        // Verify stock consistency
        SkuEntity sku = skuRepo.findById(testSkuId).orElseThrow();
        int expectedReserved = successCount.get(); // Each successful order reserves 1 item
        System.out.println("Stock: onHand=" + sku.getStockOnHand() + ", reserved=" + sku.getReservedStock());
        
        // Reserved stock should match successful orders
        assertEquals(expectedReserved, sku.getReservedStock(), 
                "Reserved stock should match successful order count");
        
        System.out.println("========================================\n");
    }
}
