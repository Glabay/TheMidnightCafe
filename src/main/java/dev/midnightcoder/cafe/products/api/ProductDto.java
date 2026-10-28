package dev.midnightcoder.cafe.products.api;

import dev.midnightcoder.cafe.products.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public record ProductDto(
    UUID productId,
    String name,
    String description,
    BigDecimal price,
    Category category,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public ProductDto(UUID productId, String name, String description, BigDecimal price, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(productId, name, description, price, null, createdAt, updatedAt);
    }

    public static ProductDto from(Product product) {
        return new ProductDto(
            product.getProductId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getCategory(),
            product.getCreatedAt(),
            product.getUpdatedAt()
        );
    }
}