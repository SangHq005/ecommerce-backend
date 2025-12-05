# Advanced Recommendation System

## Overview

The Advanced Recommendation System provides personalized product recommendations using multiple algorithms including:
- **Content-based filtering** (similar products by category, brand, price)
- **Collaborative filtering** (frequently bought together analysis)
- **Category affinity tracking** (user preference learning)
- **Trending products** (top viewed products)

## Architecture

### Components

1. **RecommendationService** - Core recommendation logic
2. **RecommendationController** - REST API endpoints
3. **UserCategoryAffinityDoc** - MongoDB document tracking user category preferences
4. **UserEventDoc** - MongoDB document for tracking user events (VIEW, PURCHASE)

### Data Storage

- **MySQL**: Product catalog, orders, SKUs
- **MongoDB**: User events, category affinity scores

## Algorithms

### 1. Personalized Recommendations

**Endpoint**: `GET /api/v1/recommendations/personalized?limit=10`

**Algorithm**:
1. Retrieve user's category affinity scores (sorted by score DESC)
2. Get top 3 categories with highest affinity
3. Fetch active products from these categories
4. Filter out recently viewed products (last 30 days)
5. Return top N products

**Affinity Score Calculation**:
```
score = (view_count * 1) + (purchase_count * 10)
```

**Fallback**: Returns trending products if user has no history

### 2. Similar Products

**Endpoint**: `GET /api/v1/recommendations/similar/{productId}?limit=6`

**Algorithm**:
1. Find all products in the same category
2. Calculate similarity score for each product:
   - Same category: +40 points
   - Same brand: +30 points
   - Similar price (±20%): +20 points
3. Sort by score DESC
4. Return top N products

**Price Comparison**:
- Uses minimum SKU price for each product
- Considers prices within ±20% range as similar

### 3. Frequently Bought Together

**Endpoint**: `GET /api/v1/recommendations/bought-together/{productId}?limit=4`

**Algorithm**:
1. Find all orders containing the target product
2. Count occurrences of other products in those orders
3. Filter products with minimum 3 co-occurrences
4. Sort by frequency DESC
5. Return top N products

**Use Case**: "Customers who bought this also bought..."

### 4. Trending Products

**Endpoint**: `GET /api/v1/recommendations/trending`

**Algorithm**:
1. Aggregate VIEW events from last 7 days
2. Group by productId and count views
3. Sort by view count DESC
4. Return top 10 products

## API Endpoints

### Track User Event

```http
POST /api/v1/recommendations/events
Authorization: Bearer {token}

?productId=123&eventType=VIEW
```

**Event Types**:
- `VIEW` - User viewed product detail page
- `PURCHASE` - User completed purchase

**Response**: 200 OK

---

### Get Trending Products

```http
GET /api/v1/recommendations/trending
```

**Response**:
```json
[
  {
    "id": 123,
    "name": "Product Name",
    "categoryId": 5,
    "brandId": 10,
    "status": "ACTIVE",
    "mainImageUrl": "https://...",
    "averageRating": 4.5,
    "reviewCount": 120
  }
]
```

---

### Get Personalized Recommendations

```http
GET /api/v1/recommendations/personalized?limit=10
Authorization: Bearer {token}
```

**Query Parameters**:
- `limit` (optional, default: 10) - Number of products to return

**Response**: Array of ProductEntity objects

---

### Get Similar Products

```http
GET /api/v1/recommendations/similar/123?limit=6
```

**Path Parameters**:
- `productId` - The reference product ID

**Query Parameters**:
- `limit` (optional, default: 6) - Number of products to return

**Response**: Array of ProductEntity objects

---

### Get Frequently Bought Together

```http
GET /api/v1/recommendations/bought-together/123?limit=4
```

**Path Parameters**:
- `productId` - The reference product ID

**Query Parameters**:
- `limit` (optional, default: 4) - Number of products to return

**Response**: Array of ProductEntity objects

## Data Models

### UserCategoryAffinityDoc

```java
{
  "id": "mongo-generated-id",
  "userId": 12345,
  "categoryId": 5,
  "viewCount": 25,
  "purchaseCount": 3,
  "updatedAt": "2025-01-04T00:00:00Z"
}
```

**Indexes**:
- userId (indexed)
- categoryId (indexed)

**Methods**:
- `incrementView()` - Increments view count by 1
- `incrementPurchase()` - Increments purchase count by 1
- `getScore()` - Returns calculated affinity score

### UserEventDoc

```java
{
  "id": "mongo-generated-id",
  "userId": 12345,
  "productId": 789,
  "eventType": "VIEW",
  "timestamp": "2025-01-04T00:00:00Z"
}
```

**Indexes**:
- userId (indexed)
- productId (indexed)
- timestamp (indexed)

## Integration Guide

### Frontend Integration

#### Track Product View

```javascript
// When user visits product detail page
fetch('/api/v1/recommendations/events?productId=123&eventType=VIEW', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

#### Show Similar Products

```javascript
// On product detail page
const response = await fetch('/api/v1/recommendations/similar/123?limit=6');
const similarProducts = await response.json();
```

#### Show Personalized Homepage

```javascript
// On homepage for logged-in users
const response = await fetch('/api/v1/recommendations/personalized?limit=20', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
const recommendations = await response.json();
```

#### Show Frequently Bought Together

```javascript
// On product detail page or cart
const response = await fetch('/api/v1/recommendations/bought-together/123?limit=4');
const bundleProducts = await response.json();
```

### Backend Integration

#### Auto-track Purchase Events

The system automatically tracks purchase events when orders are completed. This is already integrated in the payment flow:

```java
// In PaymentService.java
if (payment successful) {
    recommendationService.trackEvent(userId, productId, "PURCHASE");
}
```

## Performance Considerations

### MongoDB Indexes

Ensure these indexes are created:

```javascript
db.user_category_affinity.createIndex({ userId: 1, categoryId: 1 });
db.user_category_affinity.createIndex({ userId: 1, score: -1 });
db.user_events.createIndex({ userId: 1, eventType: 1, timestamp: -1 });
db.user_events.createIndex({ productId: 1, timestamp: -1 });
```

### Caching Recommendations

Consider caching trending products and personalized recommendations:

```java
@Cacheable(value = "trending-products", ttl = 3600)
public List<ProductEntity> getTrendingProducts() { ... }

@Cacheable(value = "personalized-recs", key = "#userId", ttl = 1800)
public List<ProductEntity> getPersonalizedRecommendations(Long userId) { ... }
```

### Query Optimization

- **Similar Products**: Pre-fetch products in batches
- **Bought Together**: Consider maintaining a pre-computed co-occurrence matrix
- **Price Comparison**: Cache min prices for products

## Testing

### Manual Testing

1. **Track Events**:
```bash
curl -X POST "http://localhost:8080/api/v1/recommendations/events?productId=1&eventType=VIEW" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

2. **Get Trending**:
```bash
curl "http://localhost:8080/api/v1/recommendations/trending"
```

3. **Get Personalized**:
```bash
curl "http://localhost:8080/api/v1/recommendations/personalized?limit=10" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

4. **Get Similar**:
```bash
curl "http://localhost:8080/api/v1/recommendations/similar/1?limit=6"
```

5. **Get Bought Together**:
```bash
curl "http://localhost:8080/api/v1/recommendations/bought-together/1?limit=4"
```

## Future Enhancements

1. **Machine Learning Integration**
   - Train models on user behavior data
   - Implement deep learning for better recommendations
   - Add A/B testing framework

2. **Real-time Processing**
   - Use Apache Kafka for event streaming
   - Implement real-time affinity score updates
   - Add Redis for hot data caching

3. **Advanced Filtering**
   - Add user demographic-based filtering
   - Implement time-of-day recommendations
   - Add seasonal trend detection

4. **Evaluation Metrics**
   - Track click-through rate (CTR)
   - Measure conversion rate from recommendations
   - Implement recommendation quality scoring

5. **Anonymous User Support**
   - Session-based recommendations for non-logged-in users
   - Cookie-based tracking (with consent)
   - Merge anonymous history on login

## Troubleshooting

### No Recommendations Returned

**Cause**: User has no interaction history

**Solution**: System automatically falls back to trending products

### Similar Products Not Matching

**Cause**: Insufficient products in same category

**Solution**: Broaden search to include products from related categories

### Performance Issues

**Cause**: Large order history for frequently bought together

**Solution**:
- Add pagination
- Implement data archiving for old orders
- Use materialized views for co-occurrence data

## Security

- Event tracking requires authentication
- Product IDs are validated before processing
- Rate limiting should be applied to prevent abuse
- Consider GDPR compliance for user event data

## Monitoring

Key metrics to monitor:
- Recommendation API response time
- Cache hit rate
- Event tracking volume
- Affinity score distribution
- Recommendation diversity
