package com.example.ecommerce.ecommerce_backend.infrastructure.persistence.mysql.repository;

import com.example.ecommerce.ecommerce_backend.api.dto.search.ProductSearchHit;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.*;

@Repository
public class SearchQueryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public SearchQueryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Page<ProductSearchHit> searchActiveProducts(
            String q,
            Long categoryId,
            Long brandId,
            Long shopId,
            Long minPrice,
            Long maxPrice,
            Boolean inStock,
            String sort,
            int page,
            int size
    ) {
        // Base SQL: aggregate SKU price range, pick thumbnail
        StringBuilder where = new StringBuilder(" WHERE p.status = 'ACTIVE' ");
        Map<String, Object> params = new HashMap<>();

        if (q != null && !q.isBlank()) {
            where.append(" AND (MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) ");
            where.append(" OR p.name LIKE :qLike ) ");
            params.put("q", toBooleanQuery(q));
            params.put("qLike", "%" + q.trim() + "%");
        }

        if (categoryId != null) { where.append(" AND p.category_id = :categoryId "); params.put("categoryId", categoryId); }
        if (brandId != null)    { where.append(" AND p.brand_id = :brandId "); params.put("brandId", brandId); }
        if (shopId != null)     { where.append(" AND p.shop_id = :shopId "); params.put("shopId", shopId); }

        // Price filters based on SKU price range
        if (minPrice != null) { where.append(" AND skuAgg.min_price >= :minPrice "); params.put("minPrice", minPrice); }
        if (maxPrice != null) { where.append(" AND skuAgg.max_price <= :maxPrice "); params.put("maxPrice", maxPrice); }

        if (inStock != null && inStock) {
            // inStock: at least one SKU stock > 0
            where.append(" AND skuAgg.total_stock > 0 ");
        }

        String orderBy = switch (sort == null ? "" : sort) {
            case "PRICE_ASC"  -> " ORDER BY skuAgg.min_price ASC ";
            case "PRICE_DESC" -> " ORDER BY skuAgg.min_price DESC ";
            case "NEWEST"     -> " ORDER BY p.created_at DESC ";
            case "RELEVANCE"  -> (q != null && !q.isBlank())
                    ? " ORDER BY MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) DESC, p.created_at DESC "
                    : " ORDER BY p.created_at DESC ";
            default -> " ORDER BY p.created_at DESC ";
        };

        String selectSql =
                "SELECT " +
                        " p.id AS product_id, p.name, p.shop_id, p.category_id, p.brand_id, " +
                        " skuAgg.min_price, skuAgg.max_price, img.thumbnail_url, p.created_at, " +
                        (q != null && !q.isBlank()
                                ? " MATCH(p.name, p.description) AGAINST (:q IN BOOLEAN MODE) AS score "
                                : " NULL AS score ") +
                        "FROM product p " +
                        "JOIN ( " +
                        "  SELECT s.product_id, MIN(s.price) AS min_price, MAX(s.price) AS max_price, SUM(CASE WHEN s.stock_on_hand > 0 THEN s.stock_on_hand ELSE 0 END) AS total_stock " +
                        "  FROM product_sku s GROUP BY s.product_id " +
                        ") skuAgg ON skuAgg.product_id = p.id " +
                        "LEFT JOIN ( " +
                        "  SELECT pi.product_id, MIN(pi.url) AS thumbnail_url " +
                        "  FROM product_image pi GROUP BY pi.product_id " +
                        ") img ON img.product_id = p.id " +
                        where +
                        orderBy +
                        " LIMIT :limit OFFSET :offset ";

        String countSql =
                "SELECT COUNT(1) AS cnt " +
                        "FROM product p " +
                        "JOIN ( " +
                        "  SELECT s.product_id, MIN(s.price) AS min_price, MAX(s.price) AS max_price, SUM(CASE WHEN s.stock_on_hand > 0 THEN s.stock_on_hand ELSE 0 END) AS total_stock " +
                        "  FROM product_sku s GROUP BY s.product_id " +
                        ") skuAgg ON skuAgg.product_id = p.id " +
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
                        rs.getTimestamp("created_at").toInstant(),
                        rs.getObject("score") == null ? null : rs.getDouble("score")
                )
        );

        return new PageImpl<>(content, PageRequest.of(page, size), total == null ? 0 : total);
    }

    // MySQL boolean mode: add * suffix for prefix match, keep it simple for local
    private String toBooleanQuery(String q) {
        String[] parts = q.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isBlank()) continue;
            sb.append("+").append(p).append("* ").toString();
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
}
