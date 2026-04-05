package com.tienda.universitaria.api.repository;


import com.tienda.universitaria.api.domain.entities.*;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderItemsTest {
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void shouldStart() {
        System.out.println("Container running " + postgreSQLContainer.isRunning());
    }

    @Autowired private OrderItemsRepository orderItemsRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired private AddressRepository addressRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Customer savedCustomer;
    private Address savedAddress;
    private Product savedProduct;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        savedCustomer = customerRepository.save(Customer.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .phone("123456789")
                .status(CustomerStatus.ACTIVE)
                .build());

        savedAddress = addressRepository.save(Address.builder()
                .street("Calle 123")
                .city("Bogotá")
                .state("Cundinamarca")
                .zip("110111")
                .country("Colombia")
                .customer(savedCustomer)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Electrónica")
                .description("Gadgets")
                .build());

        Product product = Product.builder()
                .sku("SKU-001")
                .name("Laptop")
                .description("Laptop gamer")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .category(category)
                .build();

        Inventory inventory = Inventory.builder()
                .availableStock(10)
                .minimumStock(2)
                .product(product)
                .build();

        product.setInventory(inventory);
        savedProduct = productRepository.save(product);

        savedOrder = orderRepository.save(Order.builder()
                .total(new BigDecimal("1500.00"))
                .status(OrderStatus.CREATED)
                .customer(savedCustomer)
                .address(savedAddress)
                .build());
    }

    // Helper
    private OrderItem createItem(Order order, Product product, int quantity) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .subtotal(product.getPrice().multiply(new BigDecimal(quantity)))
                .build();
        return orderItemsRepository.save(item);
    }

    // ── findByOrderId ─────────────────────────────────────────

    @Test
    void shouldFindItemsByOrderId() {
        createItem(savedOrder, savedProduct, 2);
        createItem(savedOrder, savedProduct, 1);

        List<OrderItem> items = orderItemsRepository.findByOrderId(savedOrder.getId());

        assertEquals(2, items.size());
    }

    @Test
    void shouldReturnEmptyWhenOrderHasNoItems() {
        List<OrderItem> items = orderItemsRepository.findByOrderId(UUID.randomUUID());

        assertTrue(items.isEmpty());
    }

    // ── existsByProductIdAndOrderStatusNot ────────────────────

    @Test
    void shouldReturnTrueWhenProductHasActiveOrders() {
        createItem(savedOrder, savedProduct, 1);

        boolean exists = orderItemsRepository
                .existsByProductIdAndOrderStatusNot(savedProduct.getId(), OrderStatus.CANCELLED);

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenProductOnlyHasCancelledOrders() {
        Order cancelledOrder = orderRepository.save(Order.builder()
                .total(new BigDecimal("1500.00"))
                .status(OrderStatus.CANCELLED)
                .customer(savedCustomer)
                .address(savedAddress)
                .build());

        createItem(cancelledOrder, savedProduct, 1);

        boolean exists = orderItemsRepository
                .existsByProductIdAndOrderStatusNot(savedProduct.getId(), OrderStatus.CANCELLED);

        assertFalse(exists);
    }

    // ── findBestSellingProducts ───────────────────────────────

    @Test
    void shouldReturnBestSellingProducts() {
        Order deliveredOrder = orderRepository.save(Order.builder()
                .total(new BigDecimal("3000.00"))
                .status(OrderStatus.DELIVERED)
                .customer(savedCustomer)
                .address(savedAddress)
                .build());

        createItem(deliveredOrder, savedProduct, 3);

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        List<Object[]> result = orderItemsRepository.findBestSellingProducts(from, to);

        assertFalse(result.isEmpty());
        assertEquals(3L, ((Number) result.get(0)[1]).longValue());
    }

    @Test
    void shouldNotIncludeNonDeliveredOrdersInBestSelling() {
        createItem(savedOrder, savedProduct, 5); // CREATED, no DELIVERED

        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now().plusDays(1);

        List<Object[]> result = orderItemsRepository.findBestSellingProducts(from, to);

        assertTrue(result.isEmpty());
    }
}
