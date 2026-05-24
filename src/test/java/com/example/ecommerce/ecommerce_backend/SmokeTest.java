package com.example.ecommerce.ecommerce_backend;

import com.example.ecommerce.ecommerce_backend.api.dto.auth.LoginRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.RegisterRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.auth.TokenResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.cart.AddToCartRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.catalog.SkuRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.CheckoutRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.order.OrderResponse;
import com.example.ecommerce.ecommerce_backend.api.dto.payment.CreatePaymentRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.seller.UpdateOrderStatusRequest;
import com.example.ecommerce.ecommerce_backend.application.service.auth.PasswordHasher;
import com.example.ecommerce.ecommerce_backend.domain.order.OrderStatus;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.entity.*;
import com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import com.example.ecommerce.ecommerce_backend.api.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * FINAL SMOKE TEST (12 Critical Cases)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc
public class SmokeTest extends BaseIntegrationTest {

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
    private OrderItemJpaRepository orderItemRepo;

    @Autowired
    private StockReservationJpaRepository resRepo;

    @Autowired
    private PaymentJpaRepository paymentRepo;

    @Autowired
    private CategoryJpaRepository categoryRepo;

    @Autowired
    private BrandJpaRepository brandRepo;

    @Autowired
    private SellerShopJpaRepository shopRepo;

    @Autowired
    private PasswordHasher hasher;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.EventLogMongoRepository eventLogMongoRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.UserEventMongoRepository userEventMongoRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.UserCategoryAffinityMongoRepository userCategoryAffinityMongoRepository;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mongo.repository.ChatMessageMongoRepository chatMessageMongoRepository;

    private static String clientAccessToken;
    private static String sellerAccessToken;
    private static String adminAccessToken;
    private static Long testProductId;
    private static Long testSkuId;
    private static Long testAddressId;
    private static String testOrderCode;
    private static Long testOrderId;
    private static Long testShopId;

    @Test
    @Order(1)
    @DisplayName("[1] AUTH – Register & Login")
    void test1_Auth() throws Exception {
        seedData();
        
        String email = "smoke_client_" + UUID.randomUUID().toString().substring(0, 8) + "@demo.local";
        String password = "Password123!";
        
        RegisterRequest reg = new RegisterRequest(email, password, "Smoke Client");
        
        MvcResult regResult = mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<TokenResponse> regResponse = om.readValue(regResult.getResponse().getContentAsString(), new TypeReference<ApiResponse<TokenResponse>>(){});
        TokenResponse token = regResponse.data();
        assertNotNull(token.accessToken());
        clientAccessToken = token.accessToken();

        // Login check
        LoginRequest login = new LoginRequest(email, password);
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andReturn();
        
        // Prepare shared data for further tests
        UserEntity user = userRepo.findByEmail(email).orElseThrow();
        UserAddressEntity addr = new UserAddressEntity();
        addr.setUserId(user.getId());
        addr.setReceiverName("Smoke Receiver");
        addr.setReceiverPhone("0912345678");
        addr.setLine1("Smoke Street");
        addr.setWard("Ward 1");
        addr.setDistrict("District 1");
        addr.setProvince("HCM");
        addr.setDefault(true); // Fixed: setter name
        testAddressId = addressRepo.save(addr).getId();

        // Ensure roles for step 6 & 7 actors exist and have correct passwords
        prepareActor("admin@demo.local", "ADMIN");
        prepareActor("seller1@demo.local", "SELLER");
    }

    private void prepareActor(String email, String roleCode) {
        UserEntity user = userRepo.findByEmail(email).orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setEmail(email);
            u.setFullName(roleCode + " User");
            u.setStatus("ACTIVE");
            return u;
        });
        user.setPasswordHash(hasher.hash("Password123!"));
        var role = roleRepo.findByCode(roleCode).orElseThrow();
        if (!user.getRoles().contains(role)) {
            user.getRoles().add(role);
        }
        userRepo.save(user);
    }

    @Test
    @Order(2)
    @DisplayName("[2] CATALOG – Search Product")
    void test2_SearchProduct() throws Exception {
        // The endpoint is /api/v1/products/search (viewed earlier in SearchQueryRepository/Controller?)
        // Actually Controller list said /api/v1/products/search is PUBLIC.
        mockMvc.perform(get("/api/v1/search/products")
                .param("q", "iPhone"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    @DisplayName("[3] CATALOG – View Product Detail")
    void test3_ProductDetail() throws Exception {
        ProductEntity product = productRepo.findAll().stream()
                .filter(p -> p.getStatus().equals("ACTIVE"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active products in DB"));
        testProductId = product.getId();
        testShopId = product.getShopId();
        
        List<SkuEntity> skus = skuRepo.findByProductIdOrderByIdAsc(testProductId);
        assertFalse(skus.isEmpty(), "Product should have SKUs");
        testSkuId = skus.get(0).getId();

        mockMvc.perform(get("/api/v1/catalog/public/products/" + testProductId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.product.id").value(testProductId));
    }

    @Test
    @Order(4)
    @DisplayName("[4] CART – Add Item to Cart")
    void test4_AddToCart() throws Exception {
        AddToCartRequest req = new AddToCartRequest(testSkuId, 1);
        
        mockMvc.perform(post("/api/v1/cart")
                .header("Authorization", "Bearer " + clientAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @Order(5)
    @DisplayName("[5] CHECKOUT – Create Order (Idempotent)")
    void test5_Checkout() throws Exception {
        CheckoutRequest.Item item = new CheckoutRequest.Item(testProductId, testSkuId, 1);
        CheckoutRequest req = new CheckoutRequest(List.of(item), testAddressId, "COD", "Smoke test order", null);
        
        String idemKey = UUID.randomUUID().toString();
        
        MvcResult result = mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", "Bearer " + clientAccessToken)
                .header("Idempotency-Key", idemKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        ApiResponse<List<OrderResponse>> response = om.readValue(result.getResponse().getContentAsString(), 
                new TypeReference<ApiResponse<List<OrderResponse>>>(){});
        List<OrderResponse> responses = response.data();
        
        assertFalse(responses.isEmpty());
        testOrderCode = responses.get(0).orderCode();
        testOrderId = orderRepo.findByOrderCode(testOrderCode).orElseThrow().getId();

        // Test idempotency
        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", "Bearer " + clientAccessToken)
                .header("Idempotency-Key", idemKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(6)
    @DisplayName("[6] ADMIN – View Orders")
    void test6_AdminViewOrders() throws Exception {
        LoginRequest login = new LoginRequest("admin@demo.local", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        ApiResponse<TokenResponse> response = om.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<ApiResponse<TokenResponse>>(){});
        adminAccessToken = response.data().accessToken();

        mockMvc.perform(get("/api/v1/admin/dashboard/stats")
                .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk());
    }

    @Test
    @Order(7)
    @DisplayName("[7] SELLER – Update SKU Stock")
    void test7_SellerUpdateStock() throws Exception {
        LoginRequest login = new LoginRequest("seller1@demo.local", "Password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();
        ApiResponse<TokenResponse> response = om.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<ApiResponse<TokenResponse>>(){});
        sellerAccessToken = response.data().accessToken();

        SkuEntity sku = skuRepo.findById(testSkuId).orElseThrow();
        // Update stock to 100
        SkuRequest skuReq = new SkuRequest(sku.getSkuCode(), sku.getOptionSignature(), sku.getPrice(), sku.getCompareAtPrice(), 100, true, sku.getImageUrl());
        
        mockMvc.perform(put("/api/v1/seller/products/" + sku.getProductId() + "/skus")
                .header("Authorization", "Bearer " + sellerAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(List.of(skuReq))))
                .andExpect(status().isOk());
        
        assertEquals(100, skuRepo.findById(testSkuId).get().getStockOnHand());
    }

    @Test
    @Order(8)
    @DisplayName("[8] CHECKOUT – Out Of Stock Validation")
    void test8_OutOfStock() throws Exception {
        SkuEntity sku = skuRepo.findById(testSkuId).orElseThrow();
        sku.setStockOnHand(0);
        skuRepo.save(sku);

        CheckoutRequest.Item item = new CheckoutRequest.Item(testProductId, testSkuId, 1);
        CheckoutRequest req = new CheckoutRequest(List.of(item), testAddressId, "COD", "Out of stock test", null);

        mockMvc.perform(post("/api/v1/checkout")
                .header("Authorization", "Bearer " + clientAccessToken)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(9)
    @DisplayName("[9] PAYMENT – Generate VNPay URL")
    void test9_PaymentUrl() throws Exception {
        OrderEntity order = orderRepo.findById(testOrderId).orElseThrow();
        order.setStatus("PAYMENT_PENDING");
        orderRepo.save(order);

        CreatePaymentRequest req = new CreatePaymentRequest(testOrderCode);
        
        mockMvc.perform(post("/api/v1/payment/vnpay/create")
                .header("Authorization", "Bearer " + clientAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paymentUrl").exists());
    }

    @Test
    @Order(10)
    @DisplayName("[10] ORDER – Early Cancel by Client")
    void test10_CancelOrder() throws Exception {
        mockMvc.perform(post("/api/v1/orders/" + testOrderCode + "/cancel")
                .header("Authorization", "Bearer " + clientAccessToken))
                .andExpect(status().isOk());
        
        assertEquals("CANCELLED", orderRepo.findById(testOrderId).get().getStatus());
    }

    @Test
    @Order(11)
    @DisplayName("[11] SELLER – Update Order Status")
    void test11_UpdateStatus() throws Exception {
        OrderEntity order = orderRepo.findById(testOrderId).orElseThrow();
        order.setStatus("PAID");
        orderRepo.save(order);

        UpdateOrderStatusRequest req = new UpdateOrderStatusRequest(OrderStatus.PROCESSING, "Smoke test processing", null);
        
        mockMvc.perform(put("/api/v1/seller/orders/" + testOrderId + "/status")
                .header("Authorization", "Bearer " + sellerAccessToken)
                .param("shopId", testShopId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
                .andExpect(status().isOk());
        
        assertEquals("PROCESSING", orderRepo.findById(testOrderId).get().getStatus());
    }

    @Test
    @Order(12)
    @DisplayName("[12] SYSTEM – Health Check")
    void test12_Health() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
                //.andExpect(jsonPath("$.status").value("UP"));
    }
    private void seedData() {
        paymentRepo.deleteAll();
        resRepo.deleteAll();
        orderItemRepo.deleteAll();
        orderRepo.deleteAll();
        skuRepo.deleteAll();
        productRepo.deleteAll();
        shopRepo.deleteAll();
        categoryRepo.deleteAll();
        brandRepo.deleteAll();

        // Seed Seller
        UserEntity seller = userRepo.findByEmail("seller1@demo.local").orElseGet(() -> {
            UserEntity u = new UserEntity();
            u.setEmail("seller1@demo.local");
            u.setFullName("Seed Seller");
            u.setStatus("ACTIVE");
            return u;
        });
        seller.setPasswordHash(hasher.hash("Password123!"));
        var role = roleRepo.findByCode("SELLER").orElseThrow();
        if (!seller.getRoles().contains(role)) {
            seller.getRoles().add(role);
        }
        seller = userRepo.save(seller);

        // Seed Shop
        SellerShopEntity shop = new SellerShopEntity();
        shop.setSellerUserId(seller.getId());
        shop.setShopName("Seed Shop");
        shop.setShopSlug("seed-shop");
        shop.setStatus("ACTIVE");
        shop = shopRepo.save(shop);

        // Seed Category
        CategoryEntity cat = new CategoryEntity();
        cat.setName("Electronics");
        cat.setSlug("electronics");
        cat.setPath("Electronics");
        cat = categoryRepo.save(cat);

        // Seed Brand
        BrandEntity brand = new BrandEntity();
        brand.setName("Apple");
        brand.setSlug("apple");
        brand = brandRepo.save(brand);

        // Seed Product
        ProductEntity p = new ProductEntity();
        p.setShopId(shop.getId());
        p.setSellerUserId(seller.getId());
        p.setCategoryId(cat.getId());
        p.setBrandId(brand.getId());
        p.setName("iPhone 15 Pro");
        p.setSlug("iphone-15-pro");
        p.setDescription("Latest iPhone");
        p.setStatus("ACTIVE");
        p.setPrice(25000000L);
        p.setStockQuantity(100);
        p = productRepo.save(p);

        // Seed SKU
        SkuEntity sku = new SkuEntity();
        sku.setProductId(p.getId());
        sku.setSkuCode("IP15P-BLK-256");
        sku.setOptionSignature("Color:Black,Storage:256GB");
        sku.setOptionSignatureHash(org.apache.commons.codec.digest.DigestUtils.sha256Hex("Color:Black,Storage:256GB"));
        sku.setPrice(25000000L);
        sku.setStockOnHand(1000);
        sku.setReservedStock(0);
        sku.setActive(true);
        sku = skuRepo.save(sku);
        System.out.println("DEBUG: Seeded SKU ID=" + sku.getId() + ", Stock=" + sku.getStockOnHand());
    }
}
