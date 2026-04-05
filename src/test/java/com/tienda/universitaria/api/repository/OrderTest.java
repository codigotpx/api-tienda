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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderTest {
    @Container
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Test
    void containerShouldStart() {
        System.out.println("Container running " + postgres.isRunning());
    }

    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;

    private Customer savedCustomer;
    private Address savedAddress;
    private Category savedCategory;

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

        savedCategory = categoryRepository.save(Category.builder()
                .name("Electrónica")
                .description("Gadgets")
                .build());
    }

    private Order createOrder(BigDecimal total, OrderStatus status) {
        Order order = Order.builder()
                .total(total)
                .status(status)
                .customer(savedCustomer)
                .address(savedAddress)
                .build();
        return orderRepository.save(order);
    }

    @Test
    void shouldFindOrdersByCustomerId() {
        createOrder(new BigDecimal("100.00"), OrderStatus.CREATED);
        createOrder(new BigDecimal("200.00"), OrderStatus.CREATED);

        List<Order> orders = orderRepository.findByCustomerId(savedCustomer.getId());

        assertEquals(2, orders.size());
    }

    @Test
    void shouldReturnEmptyWhenCustomerHasNoOrders() {
        List<Order> orders = orderRepository.findByCustomerId(UUID.randomUUID());

        assertTrue(orders.isEmpty());
    }

    @Test
    void shouldFilterByCustomerId() {
        createOrder(new BigDecimal("100.00"), OrderStatus.CREATED);

        List<Order> result = orderRepository.findByFilters(
                savedCustomer.getId(), null, null, null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterByStatus() {
        createOrder(new BigDecimal("100.00"), OrderStatus.CREATED);
        createOrder(new BigDecimal("200.00"), OrderStatus.PAID);

        List<Order> result = orderRepository.findByFilters(
                null, OrderStatus.PAID, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(OrderStatus.PAID, result.get(0).getStatus());
    }

    @Test
    void shouldFilterByTotalRange() {
        createOrder(new BigDecimal("50.00"), OrderStatus.CREATED);
        createOrder(new BigDecimal("150.00"), OrderStatus.CREATED);
        createOrder(new BigDecimal("300.00"), OrderStatus.CREATED);

        List<Order> result = orderRepository.findByFilters(
                null, null, null, null,
                new BigDecimal("100.00"),
                new BigDecimal("200.00"));

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTotal().compareTo(new BigDecimal("150.00")));
    }

    @Test
    void shouldReturnAllWhenAllFiltersAreNull() {
        createOrder(new BigDecimal("100.00"), OrderStatus.CREATED);
        createOrder(new BigDecimal("200.00"), OrderStatus.PAID);

        List<Order> result = orderRepository.findByFilters(
                null, null, null, null, null, null);

        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnTopCustomersByTotal() {
        createOrder(new BigDecimal("500.00"), OrderStatus.DELIVERED);
        createOrder(new BigDecimal("300.00"), OrderStatus.DELIVERED);

        List<Object[]> result = orderRepository.findTopCustomersRaw();

        assertFalse(result.isEmpty());
        assertEquals(savedCustomer.getId(), result.get(0)[0]);
    }

    @Test
    void shouldNotIncludeCancelledOrdersInTopCustomers() {
        createOrder(new BigDecimal("500.00"), OrderStatus.CANCELLED);

        List<Object[]> result = orderRepository.findTopCustomersRaw();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnTopCategoriesByVolume() {
        Product product = Product.builder()
                .sku("SKU-001")
                .name("Laptop")
                .description("Desc")
                .price(new BigDecimal("100.00"))
                .active(true)
                .category(savedCategory)
                .build();

        Inventory inventory = Inventory.builder()
                .availableStock(10)
                .minimumStock(2)
                .product(product)
                .build();

        product.setInventory(inventory);
        product = productRepository.save(product);

        Order order = createOrder(new BigDecimal("200.00"), OrderStatus.DELIVERED);

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .unitPrice(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("200.00"))
                .build();

        order.getOrderItems().add(item);
        orderRepository.save(order);

        List<Object[]> result = orderRepository.findTopCategoriesByVolumeRaw();

        assertFalse(result.isEmpty());
        assertEquals("Electrónica", result.get(0)[0]);
    }
}
