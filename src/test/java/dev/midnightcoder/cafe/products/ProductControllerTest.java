package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CartService cartService;

    private ProductController productController;

    @BeforeEach
    void setUp() {
        productController = new ProductController(productService, cartService);
    }

    @Test
    void testAddToCart_takesProductDtoAndCustomerId() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        ProductDto productDto = new ProductDto(
            productId,
            "Latte",
            "Creamy latte",
            new BigDecimal("4.50"),
            Category.BEVERAGE,
            LocalDateTime.now(),
            LocalDateTime.now()
        );

        ResponseEntity<Cart> response = productController.addToCart(productDto, customerId);

        verify(cartService).addItemToCart(productDto, customerId);
        assertEquals(HttpStatus.SEE_OTHER, response.getStatusCode());
        assertEquals(URI.create("/products/" + productId), response.getHeaders().getLocation());
    }
}
