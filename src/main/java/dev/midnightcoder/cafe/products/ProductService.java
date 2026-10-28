package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.*;
import jakarta.validation.Valid;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
@Service
@NullMarked
public class ProductService implements ProductLookup, ProductToDto {
    private final ProductRepository repository;

    ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Product> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Optional<Product> findByPrice(BigDecimal price) {
        return repository.findByPrice(price);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Override
    public List<Product> findByCategory(Category category) {
        return repository.findByCategory(category);
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public Page<Product> findByCategory(Category category, Pageable pageable) {
        return repository.findByCategory(category, pageable);
    }

    public ProductDto registerNewProduct(@Valid ProductCreationRequest request) {
        var product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        repository.save(product);
        return convert(product);
    }

    public Long count() {
        return repository.countAllBy();
    }

    public void update(UUID productId, ProductCreationRequest form) {
        var cachedProduct = repository.findById(productId);
        cachedProduct.ifPresent(product -> {
            product.setName(form.name());
            product.setDescription(form.description());
            product.setPrice(form.price());
            product.setCategory(form.category());
            repository.save(product);
        });
    }
}
