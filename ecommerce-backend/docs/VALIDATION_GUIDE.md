# Validation Guide for DTOs

This guide shows how to add proper validation to DTOs in the ecommerce-backend project.

## Common Validation Annotations

### Built-in Jakarta Validation Annotations

```java
import jakarta.validation.constraints.*;

// String validations
@NotNull        // Field cannot be null
@NotBlank       // String cannot be null, empty, or whitespace
@NotEmpty       // Collection/String cannot be null or empty
@Email          // Valid email format
@Size(min = 8, max = 100)  // String/Collection size
@Pattern(regexp = "regex")  // Matches regex pattern

// Number validations
@Positive       // Number must be > 0
@PositiveOrZero // Number must be >= 0
@Negative       // Number must be < 0
@Min(value = 0) // Minimum value
@Max(value = 100) // Maximum value
@DecimalMin(value = "0.0") // For BigDecimal
@DecimalMax(value = "100.0")

// Date/Time validations
@Past           // Date must be in the past
@PastOrPresent  // Date must be past or present
@Future         // Date must be in the future
@FutureOrPresent

// Other
@AssertTrue     // Boolean must be true
@AssertFalse    // Boolean must be false
```

### Custom Validation Annotations

```java
@ValidPhone     // Vietnamese phone number validation
```

## Example DTOs with Full Validation

### 1. Register Request

```java
package com.example.ecommerce.ecommerce_backend.api.dto.auth;

import com.example.ecommerce.ecommerce_backend.api.validation.ValidPhone;
import jakarta.validation.constraints.*;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$",
             message = "Password must contain at least one uppercase, lowercase, and digit")
    String password,

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    String fullName,

    @ValidPhone(message = "Invalid Vietnamese phone number")
    String phoneNumber
) {}
```

### 2. Add To Cart Request

```java
package com.example.ecommerce.ecommerce_backend.api.dto.cart;

import jakarta.validation.constraints.*;

public record AddToCartRequest(
    @NotNull(message = "SKU ID is required")
    @Positive(message = "SKU ID must be positive")
    Long skuId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 999, message = "Quantity cannot exceed 999")
    Integer quantity
) {}
```

### 3. Product Create Request

```java
package com.example.ecommerce.ecommerce_backend.api.dto.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateProductRequest(
    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    String name,

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    String description,

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be positive")
    Long categoryId,

    @Positive(message = "Brand ID must be positive")
    Long brandId,

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    Long price,

    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be 0 or greater")
    Integer stock,

    @Valid // Validate nested objects
    @NotEmpty(message = "At least one image is required")
    @Size(max = 10, message = "Maximum 10 images allowed")
    List<ProductImageDTO> images,

    @Size(max = 50, message = "Maximum 50 tags allowed")
    List<@NotBlank @Size(max = 50) String> tags
) {}
```

### 4. Address Request

```java
package com.example.ecommerce.ecommerce_backend.api.dto.profile;

import com.example.ecommerce.ecommerce_backend.api.validation.ValidPhone;
import jakarta.validation.constraints.*;

public record AddressRequest(
    @NotBlank(message = "Recipient name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String recipientName,

    @NotBlank(message = "Phone number is required")
    @ValidPhone
    String phoneNumber,

    @NotBlank(message = "Address line is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    String addressLine,

    @NotBlank(message = "City is required")
    @Size(max = 100)
    String city,

    @NotBlank(message = "District is required")
    @Size(max = 100)
    String district,

    @NotBlank(message = "Ward is required")
    @Size(max = 100)
    String ward,

    @Pattern(regexp = "^\\d{5,6}$", message = "Postal code must be 5-6 digits")
    String postalCode,

    @NotNull(message = "Default flag is required")
    Boolean isDefault
) {}
```

### 5. Checkout Request

```java
package com.example.ecommerce.ecommerce_backend.api.dto.order;

import jakarta.validation.constraints.*;

public record CheckoutRequest(
    @NotNull(message = "Address ID is required")
    @Positive
    Long shippingAddressId,

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    String notes,

    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Invalid coupon code format")
    @Size(max = 20)
    String couponCode,

    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(COD|VNPAY|MOMO)$", message = "Invalid payment method")
    String paymentMethod
) {}
```

## Validation in Controllers

Enable validation in controllers using `@Valid` or `@Validated`:

```java
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        // Validation happens automatically before method execution
        // If validation fails, MethodArgumentNotValidException is thrown
        // and handled by GlobalExceptionHandler
    }
}
```

## Group Validation (Optional)

For complex validation scenarios:

```java
public interface CreateValidation {}
public interface UpdateValidation {}

public record ProductRequest(
    @Null(groups = CreateValidation.class)
    @NotNull(groups = UpdateValidation.class)
    Long id,

    @NotBlank(groups = {CreateValidation.class, UpdateValidation.class})
    String name
) {}

// In controller
@PostMapping
public ResponseEntity<?> create(
        @Validated(CreateValidation.class) @RequestBody ProductRequest request) {
    // id must be null for create
}

@PutMapping("/{id}")
public ResponseEntity<?> update(
        @Validated(UpdateValidation.class) @RequestBody ProductRequest request) {
    // id must not be null for update
}
```

## Testing Validations

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRejectInvalidProduct() throws Exception {
        String invalidJson = """
            {
                "name": "",
                "price": -100
            }
            """;

        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
```

## Best Practices

1. **Always validate at API boundaries** - DTOs should have validation
2. **Use meaningful messages** - Help users understand what went wrong
3. **Validate early** - Fail fast at the controller level
4. **Don't over-validate** - Trust internal code and validated data
5. **Use custom validators** - For complex business rules
6. **Group related validations** - Use validation groups when needed
7. **Test your validations** - Write tests for validation logic

## Action Items for Existing DTOs

The following DTOs need validation added:

- ✅ `RegisterRequest` - Add password strength validation
- ✅ `AddToCartRequest` - Add quantity limits
- ✅ `AddressRequest` - Add phone validation
- ⚠️ `UpdateProfileRequest` - Need to add email/phone validation
- ⚠️ `CouponValidateRequest` - Add coupon code format validation
- ⚠️ `RefundRequest` - Add amount validation
- ⚠️ All seller/admin DTOs - Add validation for bulk operations

## References

- [Jakarta Bean Validation Specification](https://beanvalidation.org/)
- [Hibernate Validator Documentation](https://hibernate.org/validator/)
- [Spring Validation Guide](https://spring.io/guides/gs/validating-form-input/)
