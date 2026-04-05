package com.tienda.universitaria.api.repository;

import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AddressTest {
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
    private AddressRepository addressRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {

        Customer customer = Customer.builder()
                .firstName("Camilo")
                .lastName("Perez")
                .email("camilo@test.com")
                .phone("3001234567")
                .status(CustomerStatus.ACTIVE)
                .build();
        savedCustomer = customerRepository.save(customer);
    }

    @Test
    void shouldFindByCustomerId() {
        // Given
        Address address = Address.builder()
                .street("Calle 123")
                .city("Bogotá")
                .state("Cundinamarca")
                .zip("110111")
                .country("Colombia")
                .customer(savedCustomer) // Relación obligatoria
                .build();
        addressRepository.save(address);

        // When
        List<Address> addresses = addressRepository.findByCustomerId(savedCustomer.getId());

        // Then
        assertFalse(addresses.isEmpty());
        assertEquals(1, addresses.size());
        assertEquals("Calle 123", addresses.get(0).getStreet());
    }

    @Test
    void shouldCheckIfAddressExistsByCustomerId() {
        // Given
        Address address = Address.builder()
                .street("Carrera 10")
                .city("Medellín")
                .state("Antioquia")
                .zip("050001")
                .country("Colombia")
                .customer(savedCustomer)
                .build();
        Address savedAddress = addressRepository.save(address);

        // When
        boolean exists = addressRepository.existsByIdAndCustomerId(savedAddress.getId(), savedCustomer.getId());
        boolean notExists = addressRepository.existsByIdAndCustomerId(UUID.randomUUID(), savedCustomer.getId());
        // Then
        assertTrue(exists, "La dirección debería existir para este cliente");
        assertFalse(notExists, "La dirección no debería existir para un ID aleatorio");
    }
}
