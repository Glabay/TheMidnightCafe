package dev.midnightcoder.cafe.customers;

import dev.midnightcoder.cafe.customers.api.CustomerCreationRequest;
import dev.midnightcoder.cafe.customers.api.CustomerDto;
import dev.midnightcoder.cafe.products.Cart;
import dev.midnightcoder.cafe.products.CartRepository;
import dev.midnightcoder.cafe.products.CartService;
import dev.midnightcoder.cafe.products.ProductRepository;
import dev.midnightcoder.cafe.products.ProductService;
import dev.midnightcoder.cafe.products.api.Category;
import dev.midnightcoder.cafe.products.api.ProductCreationRequest;
import dev.midnightcoder.cafe.products.api.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerCartIntegrationTest {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
        productRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @Test
    void testCustomerCreationCreatesAndLinksCart() {
        CustomerCreationRequest request = new CustomerCreationRequest(
            UUID.randomUUID(),
            "John",
            "Doe",
            "john.doe@example.com",
            "123-456-7890",
            "Johnny",
            LocalDate.of(1990, 1, 1),
            100,
            true
        );

        CustomerDto createdCustomer = customerService.registerNewCustomer(request);

        assertNotNull(createdCustomer.id());
        assertNotNull(createdCustomer.cartId());

        Optional<Cart> cartOptional = cartRepository.findCartById(createdCustomer.cartId());
        assertTrue(cartOptional.isPresent());
        assertEquals(createdCustomer.id(), cartOptional.get().getCustomerId());

        Optional<Cart> cartByCustomerOptional = cartRepository.findByCustomerId(createdCustomer.id());
        assertTrue(cartByCustomerOptional.isPresent());
        assertEquals(createdCustomer.cartId(), cartByCustomerOptional.get().getId());
    }

    @Test
    void testAddProductToCartWithProductDtoAndCustomerId() {
        CustomerCreationRequest request = new CustomerCreationRequest(
            UUID.randomUUID(),
            "Jane",
            "Smith",
            "jane.smith@example.com",
            "555-123-4567",
            "Jane",
            LocalDate.of(1995, 5, 15),
            50,
            false
        );

        CustomerDto customer = customerService.registerNewCustomer(request);
        ProductDto product = productService.registerNewProduct(new ProductCreationRequest(
            "Caramel Macchiato",
            "Sweet coffee",
            new BigDecimal("4.75"),
            Category.BEVERAGE
        ));

        cartService.addItemToCart(product, customer.id());

        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customer.id());
        assertTrue(cartOptional.isPresent());
        Cart cart = cartOptional.get();
        assertEquals(1, cart.getProducts().size());
        assertEquals("Caramel Macchiato", cart.getProducts().get(0).getName());
    }
}
