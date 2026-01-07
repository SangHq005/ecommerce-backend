package com.example.ecommerce.ecommerce_backend.shared.util;

import java.text.Normalizer;
import java.util.Locale;

public final class CatalogSlugUtil {
    private CatalogSlugUtil(){}

    public static String slugify(String input) {
        String nowhitespace = input.trim().replaceAll("\\s+", "-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-zA-Z0-9\\-]", "")
                .toLowerCase(Locale.ROOT);
        return slug.isBlank() ? "item" : slug;
    }
}
