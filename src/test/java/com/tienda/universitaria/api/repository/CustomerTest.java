package com.tienda.universitaria.api.repository;

import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;


@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CustomerTest {
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

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void shouldFindByEmail() {
        Customer customer = new Customer();
        customer.setEmail("email@gmail.com");
        customer.setFirstName("Camilo");
        customer.setLastName("Cerpa");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setPhone("1234567890");
        customerRepository.save(customer);

        Customer customer2 = new Customer();
        customer2.setEmail("email2@gmail.com");
        customer2.setFirstName("Camilo2");
        customer2.setLastName("Cerpa2");
        customer2.setPhone("1234567890");
        customer2.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer2);

        Optional<Customer> found = customerRepository.findByEmail(customer.getEmail());

        assertTrue(found.isPresent());
        assertEquals(customer.getFirstName(), found.get().getFirstName());
    }

    @Test
    void shouldExistByEmail() {
        Customer customer = new Customer();
        customer.setEmail("email@gmail.com");
        customer.setFirstName("Camilo");
        customer.setLastName("Cerpa");
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setPhone("1234567890");
        customerRepository.save(customer);

        boolean exists = customerRepository.existsByEmail(customer.getEmail());

        assertTrue(exists);
    }

    @Test
    void shouldFindByStatus() {
        Customer customer = new Customer();
        customer.setEmail("juan@gmail.com");
        customer.setFirstName("Juan");
        customer.setLastName("Perez");
        customer.setPhone("3001112233");
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer);

        List<Customer> activeCustomers = customerRepository.findByStatus(CustomerStatus.ACTIVE);

        assertFalse(activeCustomers.isEmpty());
        assertEquals(CustomerStatus.ACTIVE, activeCustomers.get(0).getStatus());
    }

}
