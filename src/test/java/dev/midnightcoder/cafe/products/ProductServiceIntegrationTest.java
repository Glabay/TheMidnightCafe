package dev.midnightcoder.cafe.products;

import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductCreationRequest;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void testFindAllAndFindByCategory_returnsDistinctProducts() {
        productService.registerNewProduct(new ProductCreationRequest("Espresso", "Strong coffee", new BigDecimal("3.50"), Category.BEVERAGE));
        productService.registerNewProduct(new ProductCreationRequest("Cappuccino", "Coffee with foam", new BigDecimal("4.25"), Category.BEVERAGE));
        productService.registerNewProduct(new ProductCreationRequest("Latte", "Coffee with milk", new BigDecimal("4.50"), Category.BEVERAGE));
        productService.registerNewProduct(new ProductCreationRequest("Croissant", "Flaky pastry", new BigDecimal("3.25"), Category.BAKED_GOODS));
        productService.registerNewProduct(new ProductCreationRequest("Muffin", "Blueberry muffin", new BigDecimal("3.50"), Category.BAKED_GOODS));

        List<Product> all = productService.findAll();
        assertEquals(5, all.size());
        assertNotEquals(all.get(0).getName(), all.get(1).getName());
        assertNotEquals(all.get(1).getName(), all.get(2).getName());

        List<Product> beverages = productService.findByCategory(Category.BEVERAGE);
        assertEquals(3, beverages.size());
        assertEquals("Espresso", beverages.get(0).getName());
        assertEquals("Cappuccino", beverages.get(1).getName());
        assertEquals("Latte", beverages.get(2).getName());

        List<Product> bakedGoods = productService.findByCategory(Category.BAKED_GOODS);
        assertEquals(2, bakedGoods.size());
        assertEquals("Croissant", bakedGoods.get(0).getName());
        assertEquals("Muffin", bakedGoods.get(1).getName());

        Page<Product> beveragePage = productService.findByCategory(Category.BEVERAGE, PageRequest.of(0, 10));
        assertEquals(3, beveragePage.getContent().size());
        assertEquals("Espresso", beveragePage.getContent().get(0).getName());
        assertEquals("Cappuccino", beveragePage.getContent().get(1).getName());
        assertEquals("Latte", beveragePage.getContent().get(2).getName());
    }
}
