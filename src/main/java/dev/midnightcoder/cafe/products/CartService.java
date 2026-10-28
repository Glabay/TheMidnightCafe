package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.CartOperations;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-23
 */
@Service
public class CartService implements CartOperations {
    private final CartRepository cartRepository;
    private final ProductService productService;

    CartService(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }

    @Override
    @Transactional
    public UUID createCart(UUID customerId) {
        Cart cart = new Cart();
        cart.setCustomerId(customerId);
        return cartRepository.save(cart).getId();
    }

    @Override
    @Transactional
    public UUID createCart() {
        return cartRepository.save(new Cart()).getId();
    }

    @Transactional
    public void addItemToCart(ProductDto dto, UUID customerId) {
        var product = productService.findById(dto.productId())
            .orElseThrow(() -> new RuntimeException("Product not found"));

        var cart = cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> {
                var newCart = new Cart();
                newCart.setCustomerId(customerId);
                return cartRepository.save(newCart);
            });

        if (cart.getProducts() == null) {
            cart.setProducts(new ArrayList<>());
        }
        product.setCart(cart);
        cart.getProducts().add(product);
        cartRepository.save(cart);
    }
}
