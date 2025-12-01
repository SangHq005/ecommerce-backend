# Caching Strategy Guide

## Overview

The application uses **Redis** for distributed caching with Spring Cache abstraction.

## Cache Regions and TTLs

| Cache Name | TTL | Use Case |
|------------|-----|----------|
| `products` | 5 min | Product list queries |
| `product-details` | 10 min | Individual product details |
| `categories` | 30 min | Category list (rarely changes) |
| `brands` | 30 min | Brand list (rarely changes) |
| `user-profile` | 5 min | User profile data |
| `shop-info` | 15 min | Shop information |
| `coupons` | 10 min | Coupon validation |

## Usage Examples

### 1. Cacheable (Read)

Cache the result of a method:

```java
@Service
public class ProductService {

    @Cacheable(value = CacheConfig.CACHE_PRODUCTS, key = "#productId")
    public ProductResponse getProduct(Long productId) {
        // This will be cached. Subsequent calls with same productId
        // will return cached value without executing the method
        return productRepository.findById(productId)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    // Cache with complex key
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS,
               key = "#categoryId + ':' + #page + ':' + #size")
    public Page<ProductResponse> getProductsByCategory(
            Long categoryId, int page, int size) {
        // Cached with composite key: "categoryId:page:size"
        return productRepository.findByCategoryId(categoryId,
                PageRequest.of(page, size))
                .map(this::toResponse);
    }

    // Conditional caching
    @Cacheable(value = CacheConfig.CACHE_PRODUCTS,
               key = "#productId",
               condition = "#result != null && #result.status == 'ACTIVE'")
    public ProductResponse getActiveProduct(Long productId) {
        // Only cache if product exists and is active
        return getProduct(productId);
    }
}
```

### 2. CacheEvict (Delete)

Remove cached data when it's updated or deleted:

```java
@Service
public class ProductService {

    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#productId")
    public ProductResponse updateProduct(Long productId, UpdateProductRequest request) {
        // Update product
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // ... update fields ...

        productRepository.save(product);

        // Cache for this productId is automatically evicted
        return toResponse(product);
    }

    // Evict multiple caches
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, key = "#productId"),
        @CacheEvict(value = CacheConfig.CACHE_PRODUCT_DETAILS, key = "#productId")
    })
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    // Evict all entries in a cache
    @CacheEvict(value = CacheConfig.CACHE_PRODUCTS, allEntries = true)
    public void deleteAllProducts() {
        productRepository.deleteAll();
    }
}
```

### 3. CachePut (Update)

Update cache without evicting:

```java
@Service
public class UserService {

    @CachePut(value = CacheConfig.CACHE_USER_PROFILE, key = "#userId")
    public UserProfile updateProfile(Long userId, UpdateProfileRequest request) {
        // Method always executes, and result updates the cache
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        user.setFullName(request.fullName());
        user.setPhoneNumber(request.phoneNumber());

        return toProfile(userRepository.save(user));
    }
}
```

### 4. Caching with SpEL (Spring Expression Language)

```java
@Service
public class CouponService {

    // Cache key from object property
    @Cacheable(value = CacheConfig.CACHE_COUPONS, key = "#request.couponCode")
    public CouponValidationResponse validateCoupon(ValidateCouponRequest request) {
        // Cache key is the coupon code from request
        return validate(request);
    }

    // Cache key with method result
    @Cacheable(value = CacheConfig.CACHE_COUPONS, key = "#result.code")
    public CouponResponse createCoupon(CreateCouponRequest request) {
        // Cache key is the code from the returned object
        CouponEntity coupon = new CouponEntity();
        coupon.setCode(request.code());
        // ... set other fields ...
        return toResponse(couponRepository.save(coupon));
    }

    // Use unless to skip caching certain results
    @Cacheable(value = CacheConfig.CACHE_COUPONS,
               key = "#code",
               unless = "#result == null || #result.expired")
    public CouponResponse getCoupon(String code) {
        // Don't cache if coupon is null or expired
        return findByCode(code);
    }
}
```

### 5. Manual Cache Management

```java
@Service
@RequiredArgsConstructor
public class ProductService {

    private final CacheManager cacheManager;

    public void invalidateProductCache(Long productId) {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCTS);
        if (cache != null) {
            cache.evict(productId);
        }
    }

    public void invalidateAllProductCaches() {
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCTS);
        if (cache != null) {
            cache.clear();
        }
    }

    public void warmUpCache() {
        // Pre-populate cache with frequently accessed data
        List<ProductEntity> popularProducts = productRepository.findTop100ByOrderByViewCountDesc();
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCTS);

        if (cache != null) {
            for (ProductEntity product : popularProducts) {
                cache.put(product.getId(), toResponse(product));
            }
        }
    }
}
```

## Best Practices

### 1. Choose Appropriate TTLs

- **Short TTL (1-5 min)**: Frequently changing data (cart, user session)
- **Medium TTL (5-30 min)**: Semi-static data (products, profiles)
- **Long TTL (30+ min)**: Rarely changing data (categories, settings)

### 2. Cache Key Design

```java
// ✅ GOOD - Specific, predictable keys
@Cacheable(value = "products", key = "#productId")
@Cacheable(value = "products", key = "#category + ':' + #page")

// ❌ BAD - Too generic, causes conflicts
@Cacheable(value = "data", key = "#id")
```

### 3. Eviction Strategy

```java
// ✅ GOOD - Evict specific entries
@CacheEvict(value = "products", key = "#productId")

// ⚠️ USE CAREFULLY - Evicts entire cache
@CacheEvict(value = "products", allEntries = true)
```

### 4. Don't Cache Everything

**Cache:**
- ✅ Expensive database queries
- ✅ External API calls
- ✅ Complex computations
- ✅ Frequently read, rarely changed data

**Don't Cache:**
- ❌ User-specific real-time data
- ❌ Frequently changing data
- ❌ Large objects (>1MB)
- ❌ Security-sensitive data without encryption

### 5. Handle Cache Failures

```java
@Cacheable(value = "products", key = "#id")
public ProductResponse getProduct(Long id) {
    try {
        return productRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    } catch (Exception e) {
        // Method still works if cache fails
        log.warn("Cache operation failed, executing query", e);
        throw e;
    }
}
```

## Monitoring Cache Performance

### Enable Cache Metrics

Add to `application.yaml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,caches,metrics
  metrics:
    cache:
      enabled: true
```

### Monitor Cache Hits/Misses

```java
@Component
@RequiredArgsConstructor
public class CacheMetricsLogger {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000) // Every minute
    public void logCacheMetrics() {
        meterRegistry.getMeters().forEach(meter -> {
            if (meter.getId().getName().startsWith("cache.")) {
                log.info("Cache Metric: {} = {}",
                        meter.getId(), meter.measure());
            }
        });
    }
}
```

## Testing with Cache

```java
@SpringBootTest
@AutoConfigureTestDatabase
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCache() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
            }
        });
    }

    @Test
    void testProductCaching() {
        Long productId = 1L;

        // First call - should hit database
        ProductResponse response1 = productService.getProduct(productId);

        // Second call - should hit cache
        ProductResponse response2 = productService.getProduct(productId);

        assertSame(response1, response2); // Same object from cache

        // Verify cache contains the data
        Cache cache = cacheManager.getCache(CacheConfig.CACHE_PRODUCTS);
        assertNotNull(cache.get(productId));
    }
}
```

## Action Items

Apply caching to:
- ✅ `ProductService.getProduct()`
- ✅ `CategoryService.getAllCategories()`
- ✅ `BrandService.getAllBrands()`
- ⚠️ `UserProfileService.getProfile()`
- ⚠️ `ShopService.getShopInfo()`
- ⚠️ `CouponService.validateCoupon()`

## References

- [Spring Cache Abstraction](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#cache)
- [Redis Cache](https://redis.io/docs/manual/client-side-caching/)
