package dev.midnightcoder.cafe.products.api;

import java.util.Arrays;
import java.util.Optional;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-24
 */
public enum Category {
    BEVERAGE("Beverage"),
    BAKED_GOODS("Baked Goods");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Optional<Category> fromString(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String cleanValue = value.trim();
        return Arrays.stream(Category.values())
            .filter(cat -> cat.name().equalsIgnoreCase(cleanValue)
                || cat.name().replace("_", "").equalsIgnoreCase(cleanValue.replace(" ", "").replace("_", ""))
                || cat.displayName.equalsIgnoreCase(cleanValue))
            .findFirst();
    }
}
