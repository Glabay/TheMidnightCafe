package dev.midnightcoder.cafe.products.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public record ProductCreationRequest(
    @NotBlank(message = "Name cannot be blank")
    String name,
    @NotBlank(message = "Description cannot be blank")
    String description,
    @NotNull(message = "Price cannot be null")
    BigDecimal price,
    Category category
) {
    public ProductCreationRequest(String name, String description, BigDecimal price) {
        this(name, description, price, null);
    }
}
