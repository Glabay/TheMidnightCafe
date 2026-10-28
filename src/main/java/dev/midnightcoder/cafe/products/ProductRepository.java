package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByName(String name);
    Optional<Product> findByPrice(BigDecimal price);
    List<Product> findByCategory(Category category);
    Page<Product> findByCategory(Category category, Pageable pageable);

    Long countAllBy();
}