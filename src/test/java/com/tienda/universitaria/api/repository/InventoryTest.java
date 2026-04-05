package com.tienda.universitaria.api.repository;

import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InventoryTest {
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
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category savedCategory;

    @BeforeEach
    void setUp() {
        savedCategory = categoryRepository.save(Category.builder()
                .name("Electrónica")
                .description("Gadgets y más")
                .build());
    }

    private Product createProduct(String sku, String name, int available, int minimum) {
        Product product = Product.builder()
                .sku(sku)
                .name(name)
                .description("Descripción")
                .price(new BigDecimal("10.00"))
                .active(true)
                .category(savedCategory)
                .build();

        Inventory inventory = Inventory.builder()
                .availableStock(available)
                .minimumStock(minimum)
                .product(product)
                .build();

        product.setInventory(inventory);
        return productRepository.save(product);
    }

    @Test
    void shouldFindInventoryByProductId() {
        Product saved = createProduct("SKU-001", "Producto 1", 10, 5);

        Optional<Inventory> found = inventoryRepository.findByProductId(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(10, found.get().getAvailableStock());
        assertEquals(5, found.get().getMinimumStock());
    }

    @Test
    void shouldFindLowStockProducts() {

        createProduct("SKU-LOW", "Bajo Stock", 2, 5);

        createProduct("SKU-OK", "Stock Normal", 10, 5);

        createProduct("SKU-LIMIT", "En Límite", 5, 5);

        List<Object[]> lowStock = inventoryRepository.findLowStockProductsRaw();

        assertEquals(1, lowStock.size());
        assertEquals("SKU-LOW", lowStock.get(0)[2]);
    }

    @Test
    void shouldReturnEmptyWhenNoLowStock() {
        createProduct("SKU-OK1", "Normal 1", 10, 5);
        createProduct("SKU-OK2", "Normal 2", 8, 3);

        List<Object[]> lowStock = inventoryRepository.findLowStockProductsRaw();

        assertTrue(lowStock.isEmpty());
    }

    @Test
    void shouldReturnMultipleLowStockProducts() {
        createProduct("SKU-LOW1", "Bajo 1", 1, 5);
        createProduct("SKU-LOW2", "Bajo 2", 2, 10);
        createProduct("SKU-OK", "Normal", 10, 5);

        List<Object[]> lowStock = inventoryRepository.findLowStockProductsRaw();

        assertEquals(2, lowStock.size());
    }

}
