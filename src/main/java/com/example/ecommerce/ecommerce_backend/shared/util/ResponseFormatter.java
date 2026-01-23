package com.example.ecommerce.ecommerce_backend.shared.util;

import com.example.ecommerce.ecommerce_backend.api.dto.ai.AiAssistantResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Utility class for formatting AI assistant responses.
 */
@Component
public class ResponseFormatter {
    
    private final ObjectMapper objectMapper;
    
    public ResponseFormatter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Format product detail response in markdown.
     */
    public String formatProductDetail(AiAssistantResponse.ProductData product) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("# ").append(product.getName()).append("\n\n");
        
        // Basic Info
        sb.append("## 📋 Thông tin cơ bản\n\n");
        sb.append("| Thuộc tính | Giá trị |\n");
        sb.append("|------------|----------|\n");
        sb.append("| **ID** | ").append(product.getId()).append(" |\n");
        sb.append("| **Tên sản phẩm** | ").append(product.getName()).append(" |\n");
        sb.append("| **Giá bán** | ").append(formatPrice(product.getPrice())).append(" |\n");
        if (product.getOriginalPrice() != null && product.getOriginalPrice() > product.getPrice()) {
            sb.append("| **Giá gốc** | ~~").append(formatPrice(product.getOriginalPrice()))
                    .append("~~ |\n");
            long discount = product.getOriginalPrice() - product.getPrice();
            double discountPercent = (discount * 100.0) / product.getOriginalPrice();
            sb.append("| **Giảm giá** | **").append(String.format("%.0f%%", discountPercent))
                    .append("** (Tiết kiệm ").append(formatPrice(discount)).append(") |\n");
        }
        if (product.getRating() != null) {
            sb.append("| **Đánh giá** | ").append(product.getRating())
                    .append(" ⭐ (").append(product.getReviewCount()).append(" đánh giá) |\n");
        }
        
        // Additional info
        @SuppressWarnings("unchecked")
        Map<String, Object> additional = (Map<String, Object>) product.getSpecifications().get("_additional");
        if (additional != null) {
            if (additional.get("brand") != null) {
                sb.append("| **Thương hiệu** | ").append(additional.get("brand")).append(" |\n");
            }
            if (additional.get("category") != null) {
                sb.append("| **Danh mục** | ").append(additional.get("category")).append(" |\n");
            }
            if (additional.get("seller") != null) {
                sb.append("| **Người bán** | ").append(additional.get("seller")).append(" |\n");
            }
            if (additional.get("soldCount") != null) {
                sb.append("| **Đã bán** | ").append(additional.get("soldCount")).append(" sản phẩm |\n");
            }
        }
        
        sb.append("\n");
        
        // Specifications
        if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
            sb.append("## 🔧 Thông số kỹ thuật\n\n");
            
            for (Map.Entry<String, Object> entry : product.getSpecifications().entrySet()) {
                if (entry.getKey().equals("_additional")) continue;
                
                String groupName = entry.getKey();
                sb.append("### ").append(groupName).append("\n\n");
                
                if (entry.getValue() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> specs = (Map<String, String>) entry.getValue();
                    sb.append("| Thông số | Giá trị |\n");
                    sb.append("|----------|----------|\n");
                    
                    for (Map.Entry<String, String> spec : specs.entrySet()) {
                        sb.append("| **").append(spec.getKey()).append("** | ")
                                .append(spec.getValue() != null ? spec.getValue() : "N/A").append(" |\n");
                    }
                    sb.append("\n");
                }
            }
        }
        
        // Variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            sb.append("## 🎨 Biến thể sản phẩm\n\n");
            sb.append("| SKU | Giá | Tồn kho |\n");
            sb.append("|-----|-----|----------|\n");
            
            for (Map<String, Object> variant : product.getVariants()) {
                sb.append("| ").append(variant.get("skuCode")).append(" | ")
                        .append(formatPrice((Long) variant.get("price"))).append(" | ")
                        .append(variant.get("stock")).append(" |\n");
            }
            sb.append("\n");
        }
        
        // Images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            sb.append("## 🖼️ Hình ảnh sản phẩm\n\n");
            for (int i = 0; i < Math.min(product.getImages().size(), 4); i++) {
                sb.append("![Hình ").append(i + 1).append("](")
                        .append(product.getImages().get(i)).append(")\n\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Format comparison response in markdown.
     */
    public String formatComparison(
            List<AiAssistantResponse.ProductData> products,
            List<AiAssistantResponse.ComparisonData.Difference> differences,
            List<String> similarities,
            String recommendation) {
        
        StringBuilder sb = new StringBuilder();
        
        sb.append("# So sánh sản phẩm\n\n");
        
        // Product summary
        sb.append("## 📦 Sản phẩm được so sánh\n\n");
        for (int i = 0; i < products.size(); i++) {
            AiAssistantResponse.ProductData p = products.get(i);
            sb.append("### ").append(i + 1).append(". ").append(p.getName()).append("\n\n");
            sb.append("- **ID**: ").append(p.getId()).append("\n");
            sb.append("- **Giá**: ").append(formatPrice(p.getPrice())).append("\n");
            if (p.getRating() != null) {
                sb.append("- **Đánh giá**: ").append(p.getRating())
                        .append(" ⭐ (").append(p.getReviewCount()).append(" đánh giá)\n");
            }
            sb.append("\n");
        }
        
        // Similarities
        if (similarities != null && !similarities.isEmpty()) {
            sb.append("## ✅ Điểm giống nhau\n\n");
            for (String similarity : similarities) {
                sb.append("- ").append(similarity).append("\n");
            }
            sb.append("\n");
        }
        
        // Differences
        if (differences != null && !differences.isEmpty()) {
            sb.append("## ⚖️ Điểm khác biệt\n\n");
            
            // Create comparison table
            sb.append("| Thông số | ");
            for (int i = 0; i < products.size(); i++) {
                sb.append("Sản phẩm ").append(i + 1).append(" | ");
            }
            sb.append("\n|");
            for (int i = 0; i <= products.size(); i++) {
                sb.append("----------|");
            }
            sb.append("\n");
            
            for (AiAssistantResponse.ComparisonData.Difference diff : differences) {
                sb.append("| **").append(diff.getAttribute()).append("** | ");
                for (AiAssistantResponse.ProductData p : products) {
                    String value = diff.getValues().getOrDefault(p.getId(), "N/A");
                    sb.append(value).append(" | ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        
        // Recommendation
        if (recommendation != null && !recommendation.isEmpty()) {
            sb.append("## 💡 Khuyến nghị\n\n");
            sb.append(recommendation).append("\n");
        }
        
        return sb.toString();
    }
    
    /**
     * Format error message.
     */
    public String formatError(String error) {
        return "## ❌ Lỗi\n\n" + error;
    }
    
    /**
     * Convert markdown to HTML.
     */
    public String markdownToHtml(String markdown) {
        // Simple markdown to HTML conversion
        String html = markdown
                .replaceAll("^# (.+)$", "<h1>$1</h1>")
                .replaceAll("^## (.+)$", "<h2>$1</h2>")
                .replaceAll("^### (.+)$", "<h3>$1</h3>")
                .replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>")
                .replaceAll("~~(.+?)~~", "<del>$1</del>")
                .replaceAll("\\n", "<br/>");
        
        // Table conversion
        html = html.replaceAll("\\|(.+)\\|", "<tr><td>$1</td></tr>");
        
        return "<div class='ai-response'>" + html + "</div>";
    }
    
    /**
     * Convert markdown to plain text.
     */
    public String markdownToText(String markdown) {
        return markdown
                .replaceAll("^#+ (.+)$", "$1")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("~~(.+?)~~", "$1")
                .replaceAll("\\|", " ")
                .replaceAll("- ", "• ");
    }
    
    /**
     * Convert response to JSON.
     */
    public String toJson(AiAssistantResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (Exception e) {
            return "{\"error\": \"Failed to convert to JSON\"}";
        }
    }
    
    /**
     * Format price in Vietnamese currency.
     */
    private String formatPrice(Long price) {
        if (price == null) return "N/A";
        return String.format("%,d VNĐ", price);
    }
}
