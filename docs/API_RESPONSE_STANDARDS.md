# API Response Standards

## Overview

This document defines the unified API response contract for the E-Commerce Backend.
All endpoints MUST return responses in this format for consistency and frontend friendliness.

---

## Response Envelope Structure

### Success Response

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": "Human readable message",
  "data": { ... },
  "meta": {
    "page": 0,
    "size": 20,
    "total": 134,
    "totalPages": 7
  },
  "timestamp": "2026-01-16T09:30:00.000Z",
  "correlationId": "c-550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/v1/products"
}
```

### Error Response

```json
{
  "success": false,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed. Please check your input.",
  "errors": [
    {
      "field": "email",
      "message": "must be a valid email address"
    },
    {
      "field": "password",
      "message": "must be at least 8 characters"
    }
  ],
  "timestamp": "2026-01-16T09:30:00.000Z",
  "correlationId": "c-550e8400-e29b-41d4-a716-446655440000",
  "path": "/api/v1/auth/register"
}
```

---

## Field Specifications

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `success` | boolean | ✅ | `true` for success, `false` for error |
| `code` | string | ✅ | Business error code (see catalog below) |
| `message` | string | ✅ | Human-readable message for display |
| `data` | any | ❌ | Response payload (null on error) |
| `errors` | array | ❌ | Field-level errors (null on success) |
| `meta` | object | ❌ | Pagination info (only for lists) |
| `timestamp` | ISO-8601 | ✅ | Response generation time |
| `correlationId` | string | ✅ | Request tracking ID |
| `path` | string | ✅ | Request path |

### Rules

1. `data` MUST be `null` on error
2. `errors` MUST be `null` on success
3. `meta` only exists for paginated endpoints
4. Never return raw entities or stack traces

---

## HTTP Status Code Mapping

| HTTP Status | Business Code | Usage |
|-------------|---------------|-------|
| 200 | SUCCESS | GET / successful action |
| 201 | CREATED | Resource created |
| 204 | NO_CONTENT | Delete success (optional) |
| 400 | VALIDATION_ERROR / BAD_REQUEST | Invalid request |
| 401 | UNAUTHORIZED / AUTH_* | Missing/expired token |
| 403 | AUTH_ACCESS_DENIED / SELLER_NOT_OWNER | Permission denied |
| 404 | *_NOT_FOUND | Resource not found |
| 409 | CONFLICT / *_CONFLICT | Idempotency, stock conflict |
| 422 | BUSINESS_RULE_VIOLATION / *_INVALID | Domain rule violated |
| 429 | RATE_LIMIT_EXCEEDED | Too many requests |
| 500 | INTERNAL_ERROR | Unexpected system error |

---

## Error Code Catalog

### Authentication & Authorization

| Code | HTTP | Description |
|------|------|-------------|
| `UNAUTHORIZED` | 401 | Authentication required |
| `AUTH_INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `AUTH_TOKEN_EXPIRED` | 401 | Access token expired |
| `AUTH_TOKEN_INVALID` | 401 | Invalid access token |
| `AUTH_REFRESH_TOKEN_EXPIRED` | 401 | Refresh token expired |
| `AUTH_REFRESH_TOKEN_INVALID` | 401 | Invalid refresh token |
| `AUTH_ACCESS_DENIED` | 403 | Insufficient permissions |
| `AUTH_ACCOUNT_DISABLED` | 403 | Account disabled |
| `AUTH_EMAIL_NOT_VERIFIED` | 403 | Email not verified |

### Validation

| Code | HTTP | Description |
|------|------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed |
| `BAD_REQUEST` | 400 | Invalid request format |
| `INVALID_PARAMETER` | 400 | Invalid parameter value |

### Resource Not Found

| Code | HTTP | Description |
|------|------|-------------|
| `NOT_FOUND` | 404 | Generic not found |
| `USER_NOT_FOUND` | 404 | User not found |
| `PRODUCT_NOT_FOUND` | 404 | Product not found |
| `SKU_NOT_FOUND` | 404 | SKU not found |
| `ORDER_NOT_FOUND` | 404 | Order not found |
| `CART_ITEM_NOT_FOUND` | 404 | Cart item not found |
| `CATEGORY_NOT_FOUND` | 404 | Category not found |
| `BRAND_NOT_FOUND` | 404 | Brand not found |
| `SHOP_NOT_FOUND` | 404 | Shop not found |
| `COUPON_NOT_FOUND` | 404 | Coupon not found |
| `PAYMENT_NOT_FOUND` | 404 | Payment not found |
| `REVIEW_NOT_FOUND` | 404 | Review not found |
| `ADDRESS_NOT_FOUND` | 404 | Address not found |
| `REFUND_NOT_FOUND` | 404 | Refund not found |

### Conflicts

| Code | HTTP | Description |
|------|------|-------------|
| `CONFLICT` | 409 | Generic conflict |
| `CHECKOUT_IDEMPOTENCY_CONFLICT` | 409 | Duplicate checkout request |
| `OUT_OF_STOCK` | 409 | Product out of stock |
| `STOCK_RESERVATION_CONFLICT` | 409 | Stock reservation failed |
| `EMAIL_ALREADY_EXISTS` | 409 | Email already registered |
| `SHOP_ALREADY_EXISTS` | 409 | Shop exists for user |
| `REVIEW_ALREADY_EXISTS` | 409 | Review already submitted |

### Business Rule Violations

| Code | HTTP | Description |
|------|------|-------------|
| `BUSINESS_RULE_VIOLATION` | 422 | Generic rule violation |
| `ORDER_NOT_CANCELLABLE` | 422 | Cannot cancel order |
| `ORDER_NOT_REFUNDABLE` | 422 | Cannot refund order |
| `ORDER_ALREADY_PAID` | 422 | Order already paid |
| `ORDER_INVALID_STATE` | 422 | Invalid order state |
| `COUPON_EXPIRED` | 422 | Coupon expired |
| `COUPON_NOT_APPLICABLE` | 422 | Coupon not applicable |
| `COUPON_USAGE_LIMIT_REACHED` | 422 | Coupon limit reached |
| `COUPON_MIN_ORDER_NOT_MET` | 422 | Min order not met |
| `CART_EMPTY` | 422 | Cart is empty |
| `INSUFFICIENT_STOCK` | 422 | Not enough stock |
| `PRODUCT_INACTIVE` | 422 | Product not active |
| `SKU_INACTIVE` | 422 | SKU not active |

### Payment

| Code | HTTP | Description |
|------|------|-------------|
| `PAYMENT_REQUIRED` | 402 | Payment required |
| `PAYMENT_FAILED` | 422 | Payment processing failed |
| `PAYMENT_ALREADY_PROCESSED` | 422 | Already processed |
| `PAYMENT_SIGNATURE_INVALID` | 422 | Signature verification failed |
| `PAYMENT_AMOUNT_MISMATCH` | 422 | Amount mismatch |

### Seller

| Code | HTTP | Description |
|------|------|-------------|
| `SELLER_NOT_OWNER` | 403 | Not resource owner |
| `SELLER_SHOP_NOT_ACTIVE` | 422 | Shop not active |
| `SELLER_PRODUCT_INVALID_STATE` | 422 | Invalid product state |

### Admin

| Code | HTTP | Description |
|------|------|-------------|
| `ADMIN_PERMISSION_REQUIRED` | 403 | Admin only |

### System

| Code | HTTP | Description |
|------|------|-------------|
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | System error |
| `SERVICE_UNAVAILABLE` | 503 | Service down |

---

## Implementation Classes

### Core Classes

| Class | Responsibility |
|-------|----------------|
| `ApiResponse<T>` | Unified response envelope |
| `ApiResponse.FieldError` | Field-level validation error |
| `ApiResponse.PaginationMeta` | Pagination metadata |
| `ErrorCode` | Error code enum with HTTP mapping |
| `BusinessException` | Base exception for business errors |
| `ResponseHelper` | Utility for building responses |
| `GlobalExceptionHandler` | Centralized exception handling |

### Package Structure

```
api/
├── controller/           # REST Controllers
├── dto/                  # Request/Response DTOs
├── exception/            # Exception classes
│   ├── BusinessException.java
│   ├── GlobalExceptionHandler.java
│   └── ...
├── filter/               # Request filters
│   └── CorrelationIdFilter.java
└── response/             # Response utilities
    ├── ApiResponse.java
    ├── ErrorCode.java
    └── ResponseHelper.java
```

---

## Controller Usage Examples

### Success Response

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
    ProductResponse product = productService.findById(id);
    return ResponseHelper.ok(product);
}
```

### Created Response

```java
@PostMapping
public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
        @Valid @RequestBody CreateProductRequest request
) {
    ProductResponse created = productService.create(request);
    return ResponseHelper.created(created, "Product created successfully");
}
```

### Paginated Response

```java
@GetMapping
public ResponseEntity<ApiResponse<List<ProductResponse>>> listProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
) {
    Page<ProductResponse> products = productService.findAll(PageRequest.of(page, size));
    return ResponseHelper.page(products);
}
```

### Throwing Business Exceptions

```java
public ProductResponse findById(Long id) {
    return productRepository.findById(id)
            .map(this::toResponse)
            .orElseThrow(() -> new BusinessException(
                    ErrorCode.PRODUCT_NOT_FOUND,
                    "Product not found with ID: " + id
            ));
}
```

---

## Frontend Consumption Guide

### TypeScript Interface

```typescript
interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T | null;
  errors: FieldError[] | null;
  meta: PaginationMeta | null;
  timestamp: string;
  correlationId: string;
  path: string;
}

interface FieldError {
  field: string;
  message: string;
}

interface PaginationMeta {
  page: number;
  size: number;
  total: number;
  totalPages: number;
}
```

### Handling Responses

```typescript
async function fetchProduct(id: string): Promise<Product> {
  const response = await fetch(`/api/v1/products/${id}`);
  const result: ApiResponse<Product> = await response.json();
  
  if (!result.success) {
    // Handle by error code
    switch (result.code) {
      case 'PRODUCT_NOT_FOUND':
        throw new NotFoundError(result.message);
      case 'AUTH_TOKEN_EXPIRED':
        await refreshToken();
        return fetchProduct(id); // Retry
      default:
        throw new ApiError(result.code, result.message, result.correlationId);
    }
  }
  
  return result.data!;
}
```

### Error Display

```typescript
function showError(response: ApiResponse<unknown>) {
  // Show main message
  toast.error(response.message);
  
  // Show field errors if validation
  if (response.errors) {
    response.errors.forEach(err => {
      setFieldError(err.field, err.message);
    });
  }
  
  // Log for debugging
  console.error(`API Error [${response.code}] CID: ${response.correlationId}`);
}
```

---

## Migration Checklist

### Phase 1: Setup (✅ Completed)

- [x] Create `ApiResponse<T>` class
- [x] Create `ErrorCode` enum
- [x] Create `BusinessException` class
- [x] Create `ResponseHelper` utility
- [x] Update `GlobalExceptionHandler`

### Phase 2: Controller Migration

For each controller:

1. [ ] Import `ApiResponse` and `ResponseHelper`
2. [ ] Change return type from `ResponseEntity<DTO>` to `ResponseEntity<ApiResponse<DTO>>`
3. [ ] Replace `ResponseEntity.ok(dto)` with `ResponseHelper.ok(dto)`
4. [ ] Replace `ResponseEntity.status(201).body(dto)` with `ResponseHelper.created(dto)`
5. [ ] Replace pagination responses with `ResponseHelper.page(page)`
6. [ ] Replace exceptions with `BusinessException`

### Phase 3: Testing

- [ ] Add response shape tests for success cases
- [ ] Add response shape tests for error cases
- [ ] Verify all endpoints return correct structure
- [ ] Verify correlationId flows through

### Phase 4: Documentation

- [ ] Update Swagger examples
- [ ] Update Postman collection
- [ ] Notify frontend team

---

## Correlation ID Usage

Every request gets a unique correlation ID that:

1. Is generated or passed via `X-Correlation-Id` header
2. Is returned in response header and body
3. Is logged with all log statements
4. Should be included in bug reports

### Request Flow

```
Client Request
    ↓
CorrelationIdFilter (generate/extract ID)
    ↓
MDC.put("correlationId", id)
    ↓
Controller Processing
    ↓
Response with correlationId
```

---

## Best Practices

1. **Always use ResponseHelper** - Don't construct `ApiResponse` manually in controllers
2. **Throw BusinessException** - Use appropriate `ErrorCode` for all errors
3. **Never expose stack traces** - Let `GlobalExceptionHandler` handle it
4. **Include correlationId** - Always reference in support tickets
5. **Use specific error codes** - Avoid generic `BAD_REQUEST` when specific code exists
6. **Meaningful messages** - Messages should be displayable to end users
7. **Consistent pagination** - Always use zero-based page index
