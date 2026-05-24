package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit;

@Repository
public class SearchQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private boolean isMySQL = true;

    public SearchQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        try (var conn = jdbc.getJdbcTemplate().getDataSource().getConnection()) {
            String prod = conn.getMetaData().getDatabaseProductName();
            this.isMySQL = prod.toLowerCase().contains("mysql");
        } catch (Exception e) {
            this.isMySQL = true;
        }
    }

    public Page<ProductSearchHit> searchActiveProducts(
            String q,
            Long categoryId,
            Long brandId,
            Long shopId,
            Long minPrice,
            Long maxPrice,
            Boolean inStock,
            String locations,
            Integer minRating,
            String sort,
            int page,
            int size
    ) {
        // Base SQL: aggregate SKU price range, pick thumbnail
        StringBuilder where = new StringBuilder(" WHERE p.status = 'ACTIVE' ");
        Map<String, Object> params = new HashMap<>();

        if (q != null && !q.isBlank()) {
            if (isMySQL) {
                where.append(" AND (MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) ");
                where.append(" OR p.name LIKE :qLike ) ");
                params.put("q", toBooleanQuery(q));
            } else {
                where.append(" AND (p.name LIKE :qLike OR p.description LIKE :qLike) ");
            }
            params.put("qLike", "%" + q.trim() + "%");
        }

        if (categoryId != null) { where.append(" AND p.category_id = :categoryId "); params.put("categoryId", categoryId); }
        if (brandId != null)    { where.append(" AND p.brand_id = :brandId "); params.put("brandId", brandId); }
        if (shopId != null)     { where.append(" AND p.shop_id = :shopId "); params.put("shopId", shopId); }

        // Price filters based on SKU price range (handle NULL with COALESCE)
        if (minPrice != null) { 
            where.append(" AND COALESCE(skuAgg.min_price, p.price) >= :minPrice "); 
            params.put("minPrice", minPrice); 
        }
        if (maxPrice != null) { 
            where.append(" AND COALESCE(skuAgg.max_price, p.price) <= :maxPrice "); 
            params.put("maxPrice", maxPrice); 
        }

        if (inStock != null && inStock) {
            // inStock: at least one SKU stock > 0, or product has stock if no SKUs
            where.append(" AND (COALESCE(skuAgg.total_stock, p.stock_quantity) > 0) ");
        }

        if (minRating != null) {
            where.append(" AND p.average_rating >= :minRating ");
            params.put("minRating", minRating.doubleValue());
        }

        if (locations != null && !locations.isBlank()) {
            List<String> locList = Arrays.asList(locations.split(","));
            where.append(" AND shop.city IN (:locations) ");
            params.put("locations", locList);
        }

        String orderBy = switch (sort == null ? "" : sort) {
            case "PRICE_ASC"   -> " ORDER BY COALESCE(skuAgg.min_price, p.price) ASC ";
            case "PRICE_DESC"  -> " ORDER BY COALESCE(skuAgg.min_price, p.price) DESC ";
            case "NEWEST"      -> " ORDER BY p.created_at DESC ";
            case "DISCOUNT_DESC" -> " ORDER BY COALESCE(skuAgg.discount_percentage, 0) DESC, p.created_at DESC ";
            case "RELEVANCE"   -> (q != null && !q.isBlank() && isMySQL)
                    ? " ORDER BY MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) DESC, p.created_at DESC "
                    : " ORDER BY p.created_at DESC ";
            default -> " ORDER BY p.created_at DESC ";
        };

        String selectSql =
                "SELECT " +
                " p.id AS product_id, p.name, p.shop_id, p.category_id, p.brand_id, " +
                        " COALESCE(skuAgg.min_price, p.price) AS min_price, " +
                        " COALESCE(skuAgg.max_price, p.price) AS max_price, " +
                        " img.thumbnail_url, p.created_at, " +
                        " shop.city AS shop_city, p.average_rating, " +
                        " COALESCE(skuAgg.discount_percentage, 0) AS discount_percentage, " +
                        (q != null && !q.isBlank() && isMySQL
                                ? " MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) AS score "
                                : " NULL AS score ") +
                        "FROM product p " +
                        "LEFT JOIN ( " +
                        "  SELECT s.product_id, MIN(s.price) AS min_price, MAX(s.price) AS max_price, " +
                        "  MAX(CASE WHEN s.compare_at_price > s.price THEN ROUND((s.compare_at_price - s.price) * 100 / s.compare_at_price) ELSE 0 END) AS discount_percentage, " +
                        "  SUM(CASE WHEN s.stock_on_hand > 0 THEN s.stock_on_hand ELSE 0 END) AS total_stock " +
                        "  FROM product_sku s WHERE s.is_active = TRUE GROUP BY s.product_id " +
                        ") skuAgg ON skuAgg.product_id = p.id " +
                        "JOIN seller_shop shop ON shop.id = p.shop_id " +
                        "LEFT JOIN ( " +
                        "  SELECT pi.product_id, MIN(pi.image_url) AS thumbnail_url " +
                        "  FROM product_image pi GROUP BY pi.product_id " +
                        ") img ON img.product_id = p.id " +
                        where +
                        orderBy +
                        " LIMIT :limit OFFSET :offset ";

        String countSql =
                "SELECT COUNT(DISTINCT p.id) AS cnt " +
                        "FROM product p " +
                        "LEFT JOIN ( " +
                        "  SELECT s.product_id, MIN(s.price) AS min_price, MAX(s.price) AS max_price, SUM(CASE WHEN s.stock_on_hand > 0 THEN s.stock_on_hand ELSE 0 END) AS total_stock " +
                        "  FROM product_sku s WHERE s.is_active = TRUE GROUP BY s.product_id " +
                        ") skuAgg ON skuAgg.product_id = p.id " +
                        "JOIN seller_shop shop ON shop.id = p.shop_id " +
                        where;

        params.put("limit", size);
        params.put("offset", (long) page * size);

        Long total = jdbc.queryForObject(countSql, params, Long.class);
        List<ProductSearchHit> content = jdbc.query(selectSql, params, (rs, rowNum) ->
                new ProductSearchHit(
                        rs.getLong("product_id"),
                        rs.getString("name"),
                        rs.getLong("shop_id"),
                        rs.getLong("category_id"),
                        rs.getLong("brand_id"),
                        rs.getLong("min_price"),
                        rs.getLong("max_price"),
                        rs.getString("thumbnail_url"),
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : Instant.now(),
                        rs.getObject("score") == null ? null : rs.getDouble("score"),
                        rs.getString("shop_city"),
                        rs.getDouble("average_rating"),
                        rs.getInt("discount_percentage")
                )
        );

        return new PageImpl<>(content, PageRequest.of(page, size), total == null ? 0 : total);
    }

    // MySQL boolean mode: add * suffix for prefix match, keep it simple for local
    private String toBooleanQuery(String q) {
        // Sanitize input to prevent SQL grammar errors from special boolean mode operators
        String sanitized = q.replaceAll("[+\\-<>~*\"@()]", " ");
        String[] parts = sanitized.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            sb.append("+").append(p).append("* ");
        }
        return sb.toString().trim();
    }

    /**
     * Find product name suggestions for autocomplete
     *
     * @param query Partial query string
     * @param limit Maximum number of suggestions
     * @return List of product name suggestions
     */
    public List<String> findProductNameSuggestions(String query, int limit) {
        String sql = "SELECT DISTINCT p.name FROM product p " +
                     "WHERE p.status = 'ACTIVE' " +
                     "AND p.name LIKE :query " +
                     "ORDER BY p.name ASC " +
                     "LIMIT :limit";

        Map<String, Object> params = new HashMap<>();
        params.put("query", query + "%");
        params.put("limit", limit);

        return jdbc.query(sql, params, (rs, rowNum) -> rs.getString("name"));
    }

    /**
     * Delete old search query logs
     *
     * @param cutoffDate Delete queries older than this date
     */
    public void deleteOldQueries(java.time.LocalDateTime cutoffDate) {
      
        String sql = "DELETE FROM search_query_log WHERE created_at < :cutoffDate";
        Map<String, Object> params = new HashMap<>();
        params.put("cutoffDate", Timestamp.from(cutoffDate.atZone(java.time.ZoneId.systemDefault()).toInstant()));

        try {
            jdbc.update(sql, params);
        } catch (Exception e) {
    
        }
    }
}
