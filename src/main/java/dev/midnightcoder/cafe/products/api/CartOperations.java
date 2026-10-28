package dev.midnightcoder.cafe.products.api;

import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-10-27
 */
public interface CartOperations {
    UUID createCart(UUID customerId);
    UUID createCart();
}
