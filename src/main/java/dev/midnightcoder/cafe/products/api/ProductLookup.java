package dev.midnightcoder.cafe.products.api;

import dev.midnightcoder.cafe.products.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
public interface ProductLookup {
    Optional<Product> findByName(String name);
    Optional<Product> findByPrice(BigDecimal price);
    Optional<Product> findById(UUID id);

    List<Product> findAll();
    List<Product> findByCategory(Category category);
    Page<Product> findAll(Pageable pageable);
    Page<Product> findByCategory(Category category, Pageable pageable);
}
