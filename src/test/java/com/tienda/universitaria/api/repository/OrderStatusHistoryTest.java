package com.tienda.universitaria.api.repository;


import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class OrderStatusHistoryTest {
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

    @Autowired
    private OrderStatusHistoryRepository historyRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AddressRepository addressRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Customer customer = customerRepository.save(Customer.builder()
                .firstName("Juan")
                .lastName("Pérez")
                .email("juan@test.com")
                .phone("123456789")
                .status(CustomerStatus.ACTIVE)
                .build());

        Address address = addressRepository.save(Address.builder()
                .street("Calle 123")
                .city("Bogotá")
                .state("Cundinamarca")
                .zip("110111")
                .country("Colombia")
                .customer(customer)
                .build());

        savedOrder = orderRepository.save(Order.builder()
                .total(new BigDecimal("100.00"))
                .status(OrderStatus.CREATED)
                .customer(customer)
                .address(address)
                .build());
    }

    private OrderStatusHistory createHistory(OrderStatus previous, OrderStatus next, String notes) {
        return historyRepository.save(OrderStatusHistory.builder()
                .order(savedOrder)
                .previousStatus(previous)
                .newStatus(next)
                .notes(notes)
                .build());
    }

    @Test
    void shouldReturnHistoryForOrder() {
        createHistory(null, OrderStatus.CREATED, "Pedido creado");
        createHistory(OrderStatus.CREATED, OrderStatus.PAID, "Pago confirmado");

        List<Object[]> history = historyRepository
                .findHistoryByOrderIdRaw(savedOrder.getId());

        assertEquals(2, history.size());
        assertEquals(OrderStatus.CREATED, history.get(0)[1]);
    }

    @Test
    void shouldReturnEmptyWhenOrderHasNoHistory() {
        List<Object[]> history = historyRepository
                .findHistoryByOrderIdRaw(java.util.UUID.randomUUID());

        assertTrue(history.isEmpty());
    }
}
