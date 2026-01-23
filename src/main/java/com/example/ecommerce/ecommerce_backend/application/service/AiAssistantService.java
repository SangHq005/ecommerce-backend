package com.example.ecommerce.ecommerce_backend.application.service;

import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantRequest;
import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantResponse;
import com.example.ecommerce.ecommerce_backend.shared.util.ResponseFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * AI Assistant Service that processes queries and generates formatted responses.
 */
@Service
public class AiAssistantService {
    
    private static final Logger log = LoggerFactory.getLogger(AiAssistantService.class);
    
    private final ProductDataAggregatorService productDataAggregator;
    private final ProductCompareService productCompareService;
    private final ProductSearchService productSearchService;
    private final ResponseFormatter responseFormatter;
    private final GroqClient groqClient;
    
    // Patterns to detect query types
    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "(so sánh|compare|đối chiếu|khác biệt|giống nhau|khác nhau|so với|vs|versus)", 
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern DETAIL_PATTERN = Pattern.compile(
            "(chi tiết|thông tin|spec|thông số|đặc điểm|tính năng|mô tả|giá|giá bán|đánh giá|rating)", 
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern SEARCH_PATTERN = Pattern.compile(
            "(tìm|tim|search|kiếm|kiem|mua|bán|ban|show|hiển thị|hien thi|có|có gì|co gi)", 
            Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\\b(\\d+)\\b");
    
    public AiAssistantService(
            ProductDataAggregatorService productDataAggregator,
            ProductCompareService productCompareService,
            ProductSearchService productSearchService,
            ResponseFormatter responseFormatter,
            GroqClient groqClient) {
        this.productDataAggregator = productDataAggregator;
        this.productCompareService = productCompareService;
        this.productSearchService = productSearchService;
        this.responseFormatter = responseFormatter;
        this.groqClient = groqClient;
    }
    
    /**
     * Process AI assistant query and return formatted response.
     */
    public AiAssistantResponse processQuery(AiAssistantRequest request) {
        long startTime = System.currentTimeMillis();
        
        // Preserve original query for logging and processing
        String originalQuery = request.getQuery();
        String query = originalQuery.toLowerCase().trim();
        
        // Detect query type
        QueryType queryType = detectQueryType(query, request.getProductIds());
        
        log.debug("Processing AI query - Type: {}, Query: {}", queryType, originalQuery);
        
        AiAssistantResponse response = new AiAssistantResponse();
        AiAssistantResponse.ResponseMetadata metadata = new AiAssistantResponse.ResponseMetadata();
        metadata.setQueryType(queryType.name());
        metadata.setTimestamp(Instant.now().toString());
        
        try {
            switch (queryType) {
                case COMPARISON:
                    response = handleComparisonQuery(request, metadata);
                    break;
                case DETAIL:
                    response = handleDetailQuery(request, metadata);
                    break;
                case SEARCH:
                    response = handleSearchQuery(request, metadata);
                    break;
                default:
                    response = handleGeneralQuery(request, metadata);
            }
            
            // Ensure response has content
            if (response.getResponse() == null || response.getResponse().isEmpty()) {
                log.warn("Empty response generated for query: {}", originalQuery);
                response = handleGeneralQuery(request, metadata);
            }
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for query: {} - {}", originalQuery, e.getMessage());
            
            // Handle specific error messages
            String errorMessage = e.getMessage();
            String userMessage;
            
            if (errorMessage != null && errorMessage.contains("not active")) {
                userMessage = "Sản phẩm này hiện không khả dụng. Vui lòng tìm kiếm sản phẩm khác hoặc thử lại sau.";
            } else if (errorMessage != null && errorMessage.contains("Không tìm thấy")) {
                // Try to search instead
                try {
                    return handleSearchQuery(request, metadata);
                } catch (Exception searchError) {
                    userMessage = "Không tìm thấy sản phẩm. Vui lòng thử tìm kiếm với từ khóa khác.";
                }
            } else {
                userMessage = "Xin lỗi, " + (errorMessage != null ? errorMessage : "có lỗi xảy ra") + ". Vui lòng thử lại với thông tin khác.";
            }
            
            response.setResponse(userMessage);
            response.setFormattedResponse(responseFormatter.formatError(errorMessage));
        } catch (Exception e) {
            log.error("Error processing AI query: {}", originalQuery, e);
            // Try to provide helpful response using Groq as fallback
            try {
                String fallbackResponse = groqClient.generateTextWithProductContext(
                        "Người dùng hỏi: " + originalQuery + ". Có lỗi xảy ra khi xử lý. Hãy trả lời một cách hữu ích.",
                        null
                );
                response.setResponse(fallbackResponse);
            } catch (Exception groqError) {
                log.error("Groq fallback also failed", groqError);
                response.setResponse("Xin lỗi, tôi không thể xử lý câu hỏi này. Vui lòng thử lại hoặc mô tả rõ hơn.");
            }
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            metadata.setProcessingTimeMs(processingTime);
            response.setMetadata(metadata);
        }
        
        // Format response based on requested format
        String formatted = formatResponse(response, request.getFormat());
        response.setFormattedResponse(formatted);
        response.setFormat(request.getFormat().name());
        
        return response;
    }
    
    private QueryType detectQueryType(String query, List<Long> productIds) {
        // Clean query from context markers
        String cleanQuery = query;
        if (cleanQuery.contains("[Context:") || cleanQuery.contains("Current question:")) {
            String[] parts = cleanQuery.split("Current question:|Question:");
            if (parts.length > 1) {
                cleanQuery = parts[parts.length - 1].trim().toLowerCase();
            }
        }
        
        // If explicit product IDs provided, it's a comparison
        if (productIds != null && productIds.size() >= 2) {
            return QueryType.COMPARISON;
        }
        
        // Check for comparison intent first (most specific)
        if (COMPARISON_PATTERN.matcher(cleanQuery).find()) {
            return QueryType.COMPARISON;
        }
        
        // Check for detail intent
        if (DETAIL_PATTERN.matcher(cleanQuery).find()) {
            return QueryType.DETAIL;
        }
        
        // Check for search intent (should be checked before ID extraction)
        if (SEARCH_PATTERN.matcher(cleanQuery).find()) {
            return QueryType.SEARCH;
        }
        
        // Try to extract product IDs from query
        java.util.regex.Matcher matcher = PRODUCT_ID_PATTERN.matcher(cleanQuery);
        List<Long> extractedIds = new ArrayList<>();
        while (matcher.find()) {
            try {
                String idStr = matcher.group(1);
                // Only treat as ID if it's a reasonable product ID (not too large)
                long id = Long.parseLong(idStr);
                if (id > 0 && id < 1000000) { // Reasonable product ID range
                    extractedIds.add(id);
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        if (extractedIds.size() >= 2) {
            return QueryType.COMPARISON;
        } else if (extractedIds.size() == 1) {
            return QueryType.DETAIL;
        }
        
        // If query is short and contains product-related keywords, treat as search
        // This handles queries like "iphone 15", "galaxy s24", "dell xps"
        if (cleanQuery.length() > 2 && cleanQuery.length() < 200) {
            // Check if it looks like a product name (contains letters, possibly numbers, no special query markers)
            if (!cleanQuery.contains("context:") && 
                !cleanQuery.contains("previous conversation:") &&
                (cleanQuery.matches(".*[a-zA-Z].*") || cleanQuery.matches(".*\\d+.*"))) {
                return QueryType.SEARCH;
            }
        }
        
        return QueryType.GENERAL;
    }
    
    private AiAssistantResponse handleComparisonQuery(AiAssistantRequest request, 
                                                     AiAssistantResponse.ResponseMetadata metadata) {
        List<Long> productIds = request.getProductIds();
        
        // Extract product IDs from query if not provided
        if (productIds == null || productIds.isEmpty()) {
            productIds = extractProductIds(request.getQuery());
        }
        
        if (productIds == null || productIds.size() < 2) {
            throw new IllegalArgumentException("Cần ít nhất 2 sản phẩm để so sánh");
        }
        
        // Get comprehensive product data
        List<String> productIdStrings = productIds.stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        List<AiAssistantResponse.ProductData> products = 
                productDataAggregator.getMultipleProductData(productIdStrings);
        
        // Build comparison response
        AiAssistantResponse response = new AiAssistantResponse();
        response.setProducts(products);
        
        AiAssistantResponse.ComparisonData comparison = new AiAssistantResponse.ComparisonData();
        comparison.setProducts(products);
        
        // Find differences
        List<AiAssistantResponse.ComparisonData.Difference> differences = findDifferences(products);
        comparison.setDifferences(differences);
        
        // Find similarities
        List<String> similarities = findSimilarities(products);
        comparison.setSimilarities(similarities);
        
        // Generate recommendation
        String recommendation = generateComparisonRecommendation(products, differences);
        comparison.setRecommendation(recommendation);
        
        response.setComparison(comparison);
        
        // Generate formatted text response
        String textResponse = responseFormatter.formatComparison(products, differences, similarities, recommendation);
        response.setResponse(textResponse);
        
        metadata.setProductCount(products.size());
        
        return response;
    }
    
    private AiAssistantResponse handleDetailQuery(AiAssistantRequest request,
                                                  AiAssistantResponse.ResponseMetadata metadata) {
        Long productId = null;
        
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            productId = request.getProductIds().get(0);
        } else {
            List<Long> extractedIds = extractProductIds(request.getQuery());
            if (extractedIds != null && !extractedIds.isEmpty()) {
                productId = extractedIds.get(0);
            }
        }
        
        // If no product ID found, try to search by product name
        if (productId == null) {
            String query = request.getQuery();
            
            // Clean query from context markers
            String cleanQuery = query;
            if (cleanQuery.contains("[Context:") || cleanQuery.contains("Current question:")) {
                String[] parts = cleanQuery.split("Current question:|Question:");
                if (parts.length > 1) {
                    cleanQuery = parts[parts.length - 1].trim();
                }
            }
            
            // Remove detail keywords to get product name
            String productName = cleanQuery
                    .replaceAll("(?i)(thông tin|thong tin|chi tiết|chi tiet|thông số|thong so|spec|mô tả|mo ta|về|ve|context:|previous conversation:)\\s*", "")
                    .replaceAll("\\[.*?\\]", "") // Remove any remaining brackets
                    .trim();
            
            if (productName.isEmpty() || productName.length() < 2) {
                productName = cleanQuery;
            }
            
            log.debug("No product ID found, searching by name: '{}' (from query: '{}')", productName, query);
            
            try {
                // Search for products by name - get more results to verify match
                com.example.ecommerce.ecommerce_backend.api.dto.search.PageResponse<
                        com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit> searchResult = 
                        productSearchService.search(productName, null, null, null, null, null, null, null, null, "relevance", 0, 5);
                
                if (searchResult != null && searchResult.content() != null && !searchResult.content().isEmpty()) {
                    // Find best match by checking product name similarity
                    String searchLower = productName.toLowerCase().trim();
                    com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit bestMatch = null;
                    int bestScore = 0;
                    
                    for (com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit hit : searchResult.content()) {
                        if (hit.name() == null || hit.productId() == null) continue;
                        
                        String hitName = hit.name().toLowerCase();
                        int score = calculateNameSimilarity(searchLower, hitName);
                        
                        if (score > bestScore) {
                            bestScore = score;
                            bestMatch = hit;
                        }
                    }
                    
                    // Only use if similarity is good enough (at least 30% match)
                    if (bestMatch != null && bestScore >= 30) {
                        productId = bestMatch.productId();
                        log.debug("Found matching product ID {} (name: '{}', score: {}) for search: '{}'", 
                                productId, bestMatch.name(), bestScore, productName);
                    } else {
                        log.debug("No good match found (best score: {}) for search: '{}', redirecting to search query", 
                                bestScore, productName);
                        // No good match, redirect to search query to show all results
                        return handleSearchQuery(request, metadata);
                    }
                } else {
                    // No product found, redirect to search query instead
                    log.debug("No product found for name: '{}', redirecting to search", productName);
                    return handleSearchQuery(request, metadata);
                }
            } catch (Exception e) {
                log.warn("Error searching product by name: '{}'", productName, e);
                // Fallback to search query
                return handleSearchQuery(request, metadata);
            }
        }
        
        if (productId == null) {
            // Still no product ID, treat as search query
            return handleSearchQuery(request, metadata);
        }
        
        try {
            AiAssistantResponse.ProductData productData = 
                    productDataAggregator.getComprehensiveProductData(String.valueOf(productId));
            
            AiAssistantResponse response = new AiAssistantResponse();
            response.setProducts(List.of(productData));
            
            String textResponse = responseFormatter.formatProductDetail(productData);
            response.setResponse(textResponse);
            
            metadata.setProductCount(1);
            
            return response;
            
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("not active")) {
                log.warn("Product {} is not active", productId);
                // Product exists but not active, try to search for similar products
                String query = request.getQuery();
                String productName = query
                        .replaceAll("(?i)(thông tin|thong tin|chi tiết|chi tiet|thông số|thong so|spec|mô tả|mo ta|về|ve)\\s*", "")
                        .trim();
                
                // Use Groq to provide helpful response
                String aiResponse = groqClient.generateTextWithProductContext(
                        "Người dùng hỏi về sản phẩm: " + productName + 
                        ". Sản phẩm này hiện không khả dụng (không active). " +
                        "Hãy gợi ý các sản phẩm tương tự hoặc cách tìm kiếm khác.",
                        null
                );
                
                AiAssistantResponse response = new AiAssistantResponse();
                response.setResponse(aiResponse);
                metadata.setProductCount(0);
                return response;
            } else {
                // Other IllegalArgumentException, re-throw
                throw e;
            }
        } catch (Exception e) {
            log.error("Error getting product details for ID: {}", productId, e);
            // Fallback to search
            return handleSearchQuery(request, metadata);
        }
    }
    
    private AiAssistantResponse handleSearchQuery(AiAssistantRequest request,
                                                  AiAssistantResponse.ResponseMetadata metadata) {
        AiAssistantResponse response = new AiAssistantResponse();
        
        // Extract search query from request - handle context from frontend
        String fullQuery = request.getQuery();
        
        // Extract actual search term from query (remove context markers)
        String searchQuery = fullQuery;
        
        // Remove context markers from frontend
        if (searchQuery.contains("[Context:") || searchQuery.contains("Current question:")) {
            // Extract the actual question
            String[] parts = searchQuery.split("Current question:|Question:");
            if (parts.length > 1) {
                searchQuery = parts[parts.length - 1].trim();
            }
        }
        
        // Remove search keywords to get the actual product name/query
        String cleanedQuery = searchQuery
                .replaceAll("(?i)(tìm|tim|search|kiếm|kiem|mua|bán|ban|show|hiển thị|hien thi|có|có gì|co gi|về|ve|cho|cho tôi|cho minh|context:|previous conversation:)\\s*", "")
                .replaceAll("\\[.*?\\]", "") // Remove any remaining brackets
                .trim();
        
        if (cleanedQuery.isEmpty() || cleanedQuery.length() < 2) {
            cleanedQuery = searchQuery; // Fallback to original
        }
        
        log.debug("Search query extracted: '{}' from original: '{}'", cleanedQuery, fullQuery);
        
        try {
            // Search products
            com.example.ecommerce.ecommerce_backend.api.dto.search.PageResponse<
                    com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit> searchResult = 
                    productSearchService.search(cleanedQuery, null, null, null, null, null, null, null, null, "relevance", 0, 5);
            
            if (searchResult == null) {
                throw new IllegalStateException("Search service returned null");
            }
            
            if (searchResult.content() == null || searchResult.content().isEmpty()) {
                // No results found, use Groq to provide helpful response
                log.info("No products found for query: {}", cleanedQuery);
                String aiResponse = groqClient.generateTextWithProductContext(
                        "Người dùng tìm kiếm: " + cleanedQuery + ". Không tìm thấy sản phẩm nào trong hệ thống. " +
                        "Hãy gợi ý cách tìm kiếm khác, các từ khóa liên quan, hoặc các danh mục sản phẩm có thể có.",
                        null
                );
                response.setResponse(aiResponse);
                metadata.setProductCount(0);
                return response;
            }
            
            // Convert search results to ProductData
            List<AiAssistantResponse.ProductData> products = searchResult.content().stream()
                    .map(hit -> {
                        try {
                            AiAssistantResponse.ProductData productData = new AiAssistantResponse.ProductData();
                            productData.setId(hit.productId() != null ? hit.productId() : 0L);
                            productData.setName(hit.name() != null ? hit.name() : "Sản phẩm không tên");
                            productData.setSlug(""); // Slug not available in search hit
                            productData.setPrice(hit.minPrice() > 0 ? hit.minPrice() : 0L);
                            productData.setOriginalPrice(hit.maxPrice() > hit.minPrice() ? hit.maxPrice() : null);
                            productData.setRating(hit.averageRating() != null ? BigDecimal.valueOf(hit.averageRating()) : null);
                            productData.setReviewCount(null); // Review count not available in search hit
                            productData.setImages(hit.thumbnailUrl() != null ? List.of(hit.thumbnailUrl()) : List.of());
                            return productData;
                        } catch (Exception e) {
                            log.warn("Error converting search hit to ProductData", e);
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (products.isEmpty()) {
                throw new IllegalStateException("No valid products found after conversion");
            }
            
            response.setProducts(products);
            
            // Format search results
            StringBuilder resultText = new StringBuilder();
            resultText.append("🔍 Tìm thấy **").append(searchResult.totalElements()).append("** sản phẩm:\n\n");
            
            for (int i = 0; i < products.size(); i++) {
                AiAssistantResponse.ProductData p = products.get(i);
                resultText.append(String.format("**%d. %s**\n", i + 1, p.getName()));
                resultText.append(String.format("   💰 Giá: %s\n", formatPrice(p.getPrice())));
                if (p.getRating() != null) {
                    resultText.append(String.format("   ⭐ Đánh giá: %s (%d đánh giá)\n", 
                            p.getRating().toString(), p.getReviewCount() != null ? p.getReviewCount() : 0));
                }
                resultText.append(String.format("   🔗 [Xem chi tiết](/products/%d)\n\n", p.getId()));
            }
            
            if (searchResult.totalElements() > products.size()) {
                resultText.append(String.format("... và **%d** sản phẩm khác. Hãy tìm kiếm cụ thể hơn để xem thêm.\n", 
                        searchResult.totalElements() - products.size()));
            }
            
            response.setResponse(resultText.toString());
            metadata.setProductCount(products.size());
            
        } catch (Exception e) {
            log.error("Error searching products for query: {}", cleanedQuery, e);
            // Fallback to Groq AI with better context
            try {
                String aiResponse = groqClient.generateTextWithProductContext(
                        "Người dùng muốn tìm sản phẩm: " + cleanedQuery + 
                        ". Có lỗi xảy ra khi tìm kiếm. Hãy trả lời một cách hữu ích và gợi ý cách tìm kiếm khác.",
                        null
                );
                response.setResponse(aiResponse);
            } catch (Exception groqError) {
                log.error("Groq fallback also failed", groqError);
                response.setResponse("Xin lỗi, có lỗi xảy ra khi tìm kiếm sản phẩm \"" + cleanedQuery + 
                        "\". Vui lòng thử lại hoặc tìm kiếm với từ khóa khác.");
            }
        }
        
        return response;
    }
    
    private AiAssistantResponse handleGeneralQuery(AiAssistantRequest request,
                                                   AiAssistantResponse.ResponseMetadata metadata) {
        AiAssistantResponse response = new AiAssistantResponse();
        
        // Use Groq AI for general queries
        String query = request.getQuery();
        
        // Build product context if product IDs are provided
        String productContext = null;
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            try {
                List<String> productIdStrings = request.getProductIds().stream()
                        .map(String::valueOf)
                        .collect(java.util.stream.Collectors.toList());
                List<AiAssistantResponse.ProductData> products = 
                        productDataAggregator.getMultipleProductData(productIdStrings);
                
                if (!products.isEmpty()) {
                    StringBuilder contextBuilder = new StringBuilder();
                    for (AiAssistantResponse.ProductData product : products) {
                        contextBuilder.append(String.format(
                                "- **%s** (ID: %d): Giá %s, Đánh giá %s ⭐ (%d đánh giá)\n",
                                product.getName(),
                                product.getId(),
                                formatPrice(product.getPrice()),
                                product.getRating() != null ? product.getRating().toString() : "N/A",
                                product.getReviewCount() != null ? product.getReviewCount() : 0
                        ));
                    }
                    productContext = contextBuilder.toString();
                }
            } catch (Exception e) {
                // If product data fetch fails, continue without context
                log.warn("Failed to fetch product context for AI query", e);
            }
        }
        
        // Generate AI response using Groq
        String aiResponse = groqClient.generateTextWithProductContext(query, productContext);
        response.setResponse(aiResponse);
        
        return response;
    }
    
    private List<Long> extractProductIds(String query) {
        java.util.regex.Matcher matcher = PRODUCT_ID_PATTERN.matcher(query);
        List<Long> ids = new ArrayList<>();
        while (matcher.find()) {
            try {
                ids.add(Long.parseLong(matcher.group(1)));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        return ids.isEmpty() ? null : ids;
    }
    
    private List<AiAssistantResponse.ComparisonData.Difference> findDifferences(
            List<AiAssistantResponse.ProductData> products) {
        List<AiAssistantResponse.ComparisonData.Difference> differences = new ArrayList<>();
        
        if (products.size() < 2) {
            return differences;
        }
        
        // Compare prices
        Set<Long> prices = products.stream()
                .map(p -> p.getPrice())
                .collect(Collectors.toSet());
        if (prices.size() > 1) {
            AiAssistantResponse.ComparisonData.Difference diff = new AiAssistantResponse.ComparisonData.Difference();
            diff.setAttribute("Giá bán");
            Map<Long, String> values = new HashMap<>();
            products.forEach(p -> values.put(p.getId(), formatPrice(p.getPrice())));
            diff.setValues(values);
            differences.add(diff);
        }
        
        // Compare ratings
        Set<BigDecimal> ratings = products.stream()
                .map(p -> p.getRating())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (ratings.size() > 1) {
            AiAssistantResponse.ComparisonData.Difference diff = new AiAssistantResponse.ComparisonData.Difference();
            diff.setAttribute("Đánh giá");
            Map<Long, String> values = new HashMap<>();
            products.forEach(p -> values.put(p.getId(), 
                    p.getRating() != null ? p.getRating().toString() + " ⭐" : "N/A"));
            diff.setValues(values);
            differences.add(diff);
        }
        
        // Compare specifications
        if (products.size() == 2) {
            Map<String, Object> specs1 = products.get(0).getSpecifications();
            Map<String, Object> specs2 = products.get(1).getSpecifications();
            
            compareSpecifications(specs1, specs2, products.get(0).getId(), 
                    products.get(1).getId(), differences);
        }
        
        return differences;
    }
    
    @SuppressWarnings("unchecked")
    private void compareSpecifications(Map<String, Object> specs1, Map<String, Object> specs2,
                                      Long id1, Long id2,
                                      List<AiAssistantResponse.ComparisonData.Difference> differences) {
        Set<String> allKeys = new HashSet<>();
        allKeys.addAll(specs1.keySet());
        allKeys.addAll(specs2.keySet());
        
        for (String key : allKeys) {
            if (key.equals("_additional")) continue;
            
            Object val1 = specs1.get(key);
            Object val2 = specs2.get(key);
            
            if (val1 instanceof Map && val2 instanceof Map) {
                Map<String, String> map1 = (Map<String, String>) val1;
                Map<String, String> map2 = (Map<String, String>) val2;
                
                for (String attrKey : map1.keySet()) {
                    String v1 = map1.get(attrKey);
                    String v2 = map2.getOrDefault(attrKey, "N/A");
                    
                    if (!Objects.equals(v1, v2)) {
                        AiAssistantResponse.ComparisonData.Difference diff = 
                                new AiAssistantResponse.ComparisonData.Difference();
                        diff.setAttribute(attrKey);
                        Map<Long, String> values = new HashMap<>();
                        values.put(id1, v1 != null ? v1 : "N/A");
                        values.put(id2, v2 != null ? v2 : "N/A");
                        diff.setValues(values);
                        differences.add(diff);
                    }
                }
            }
        }
    }
    
    private List<String> findSimilarities(List<AiAssistantResponse.ProductData> products) {
        List<String> similarities = new ArrayList<>();
        
        if (products.size() < 2) {
            return similarities;
        }
        
        // Check if same category
        String category1 = extractCategory(products.get(0));
        String category2 = extractCategory(products.get(1));
        if (category1 != null && category1.equals(category2)) {
            similarities.add("Cùng danh mục: " + category1);
        }
        
        // Check if same brand
        String brand1 = extractBrand(products.get(0));
        String brand2 = extractBrand(products.get(1));
        if (brand1 != null && brand1.equals(brand2)) {
            similarities.add("Cùng thương hiệu: " + brand1);
        }
        
        return similarities;
    }
    
    @SuppressWarnings("unchecked")
    private String extractCategory(AiAssistantResponse.ProductData product) {
        Map<String, Object> additional = (Map<String, Object>) product.getSpecifications().get("_additional");
        return additional != null ? (String) additional.get("category") : null;
    }
    
    @SuppressWarnings("unchecked")
    private String extractBrand(AiAssistantResponse.ProductData product) {
        Map<String, Object> additional = (Map<String, Object>) product.getSpecifications().get("_additional");
        return additional != null ? (String) additional.get("brand") : null;
    }
    
    private String generateComparisonRecommendation(
            List<AiAssistantResponse.ProductData> products,
            List<AiAssistantResponse.ComparisonData.Difference> differences) {
        if (products.size() < 2) {
            return "Cần ít nhất 2 sản phẩm để đưa ra khuyến nghị.";
        }
        
        AiAssistantResponse.ProductData p1 = products.get(0);
        AiAssistantResponse.ProductData p2 = products.get(1);
        
        StringBuilder recommendation = new StringBuilder();
        
        // Price comparison
        if (p1.getPrice() < p2.getPrice()) {
            recommendation.append("**" + p1.getName() + "** có giá tốt hơn (" + 
                    formatPrice(p1.getPrice()) + " vs " + formatPrice(p2.getPrice()) + ").\n");
        } else if (p2.getPrice() < p1.getPrice()) {
            recommendation.append("**" + p2.getName() + "** có giá tốt hơn (" + 
                    formatPrice(p2.getPrice()) + " vs " + formatPrice(p1.getPrice()) + ").\n");
        }
        
        // Rating comparison
        if (p1.getRating() != null && p2.getRating() != null) {
            if (p1.getRating().compareTo(p2.getRating()) > 0) {
                recommendation.append("**" + p1.getName() + "** có đánh giá cao hơn (" + 
                        p1.getRating() + " ⭐ vs " + p2.getRating() + " ⭐).\n");
            } else if (p2.getRating().compareTo(p1.getRating()) > 0) {
                recommendation.append("**" + p2.getName() + "** có đánh giá cao hơn (" + 
                        p2.getRating() + " ⭐ vs " + p1.getRating() + " ⭐).\n");
            }
        }
        
        if (recommendation.length() == 0) {
            recommendation.append("Cả hai sản phẩm đều có ưu điểm riêng. " +
                    "Hãy xem xét các thông số kỹ thuật chi tiết để đưa ra quyết định phù hợp.");
        }
        
        return recommendation.toString();
    }
    
    private String formatPrice(Long price) {
        if (price == null) return "N/A";
        return String.format("%,d VNĐ", price);
    }
    
    /**
     * Calculate similarity score between search query and product name.
     * Returns a score from 0-100, where 100 is perfect match.
     */
    private int calculateNameSimilarity(String searchQuery, String productName) {
        if (searchQuery == null || productName == null) return 0;
        
        String query = searchQuery.toLowerCase().trim();
        String name = productName.toLowerCase().trim();
        
        // Exact match
        if (name.equals(query)) return 100;
        
        // Contains match
        if (name.contains(query)) return 80;
        if (query.contains(name)) return 70;
        
        // Word-by-word matching
        String[] queryWords = query.split("\\s+");
        String[] nameWords = name.split("\\s+");
        
        int matchedWords = 0;
        int totalWords = Math.max(queryWords.length, nameWords.length);
        
        for (String qWord : queryWords) {
            if (qWord.length() < 2) continue; // Skip short words
            for (String nWord : nameWords) {
                if (nWord.contains(qWord) || qWord.contains(nWord)) {
                    matchedWords++;
                    break;
                }
            }
        }
        
        // Calculate score based on word matches
        int wordScore = totalWords > 0 ? (matchedWords * 100 / totalWords) : 0;
        
        // Check for key brand/model matches (e.g., "galaxy", "s24", "ultra")
        int keyMatchScore = 0;
        for (String qWord : queryWords) {
            if (qWord.length() >= 2 && name.contains(qWord)) {
                keyMatchScore += 20;
            }
        }
        keyMatchScore = Math.min(keyMatchScore, 50); // Cap at 50
        
        return Math.max(wordScore, keyMatchScore);
    }
    
    private String formatResponse(AiAssistantResponse response, AiAssistantRequest.ResponseFormat format) {
        switch (format) {
            case MARKDOWN:
                return response.getResponse(); // Already in markdown
            case HTML:
                return responseFormatter.markdownToHtml(response.getResponse());
            case JSON:
                return responseFormatter.toJson(response);
            default:
                return responseFormatter.markdownToText(response.getResponse());
        }
    }
    
    private enum QueryType {
        COMPARISON, DETAIL, SEARCH, GENERAL
    }
}
