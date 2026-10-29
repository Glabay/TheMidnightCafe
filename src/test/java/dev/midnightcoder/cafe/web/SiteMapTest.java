package dev.midnightcoder.cafe.web;

import dev.midnightcoder.cafe.products.Product;
import dev.midnightcoder.cafe.products.ProductService;
import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SiteMapTest {

    @Mock
    private ProductService productService;

    private SiteMap siteMap;

    @BeforeEach
    void setUp() {
        siteMap = new SiteMap(productService);
    }

    @Test
    void getProducts_withCategoryFilter_returnsOnlyCategoryProductsFromPage() {
        Product beverage1 = createProduct("Espresso", Category.BEVERAGE, new BigDecimal("3.50"));
        Product beverage2 = createProduct("Latte", Category.BEVERAGE, new BigDecimal("4.50"));
        Page<Product> beveragePage = new PageImpl<>(List.of(beverage1, beverage2), PageRequest.of(0, 20), 2);

        when(productService.findByCategory(eq(Category.BEVERAGE), any(Pageable.class)))
                .thenReturn(beveragePage);

        Model model = new ConcurrentModel();
        String view = siteMap.getProducts(1, 20, "BEVERAGE", "name,asc", model);

        assertEquals("products", view);
        @SuppressWarnings("unchecked")
        List<ProductDto> products = (List<ProductDto>) model.getAttribute("products");
        assertEquals(2, products.size());
        assertEquals("Espresso", products.get(0).name());
        assertEquals("Latte", products.get(1).name());
        assertEquals(2L, model.getAttribute("totalElements"));
        assertEquals("BEVERAGE", model.getAttribute("selectedCategory"));
    }

    @Test
    void getProducts_withoutCategoryFilter_returnsPagedProducts() {
        Product product1 = createProduct("Espresso", Category.BEVERAGE, new BigDecimal("3.50"));
        Product product2 = createProduct("Croissant", Category.BAKED_GOODS, new BigDecimal("3.25"));
        Page<Product> allPage = new PageImpl<>(List.of(product1, product2), PageRequest.of(0, 20), 2);

        when(productService.findAll(any(Pageable.class))).thenReturn(allPage);

        Model model = new ConcurrentModel();
        String view = siteMap.getProducts(1, 20, null, "name,asc", model);

        assertEquals("products", view);
        @SuppressWarnings("unchecked")
        List<ProductDto> products = (List<ProductDto>) model.getAttribute("products");
        assertEquals(2, products.size());
        assertEquals("Espresso", products.get(0).name());
        assertEquals("Croissant", products.get(1).name());
    }

    private Product createProduct(String name, Category category, BigDecimal price) {
        Product product = new Product();
        product.setName(name);
        product.setCategory(category);
        product.setPrice(price);
        product.setDescription("Test description for " + name);
        return product;
    }
}
