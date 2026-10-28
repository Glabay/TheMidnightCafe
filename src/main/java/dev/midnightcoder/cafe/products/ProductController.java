package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.ProductCreationRequest;
import dev.midnightcoder.cafe.products.api.ProductDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * @author Glabay | The Midnight Coder
 * @project TheMidnightCafe
 * @social Discord: Glabay
 * @website <a href="https://midnightcoder.dev">Midnight Coder</a>
 * @since 2026-08-18
 */
@RestController
@RequestMapping("/api/v1/products")
class ProductController {
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService service;
    private final CartService cartService;

    ProductController(ProductService service, CartService cartService) {
        this.service = service;
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<ProductDto> create(@Valid @RequestBody ProductCreationRequest request,
                                            BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.error("Validation errors: {}", bindingResult.getAllErrors());
            return ResponseEntity.badRequest().build();
        }
        var cachedProduct = service.registerNewProduct(request);

        return ResponseEntity.ok(cachedProduct);
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        return ResponseEntity.ok(service.findAll().stream()
                                     .map(ProductDto::from)
                                     .toList());
    }

    @PostMapping("/cart/{customerId}")
    public ResponseEntity<Cart> addToCart(@ModelAttribute ProductDto productDto,
                                          @PathVariable UUID customerId
    ) {
        cartService.addItemToCart(productDto, customerId);
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create("/products/".concat(productDto.productId().toString())))
            .build();
    }
}
