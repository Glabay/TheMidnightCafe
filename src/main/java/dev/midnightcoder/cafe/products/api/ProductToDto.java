package dev.midnightcoder.cafe.products.api;

import dev.midnightcoder.cafe.products.Product;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public interface ProductToDto {
    default ProductDto convert(Product product) {
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
