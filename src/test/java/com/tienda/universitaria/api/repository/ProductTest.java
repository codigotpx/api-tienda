package com.tienda.universitaria.api.repository;

import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
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
public class ProductTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
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
    private ProductRepository productRepository;

    private Category savedCategory;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        Category category = Category.builder()
                .name("Electrónica")
                .description("Gadgets y más")
                .build();
        savedCategory = categoryRepository.save(category);
    }

    @Test
    void shouldSaveProductWithInventory() {
        Product product = Product.builder()
                .sku("LAP-001")
                .name("Laptop Gamer")
                .description("Poderosa laptop")
                .price(new BigDecimal("1500.00"))
                .active(true)
                .category(savedCategory)
                .build();

        Inventory inventory = Inventory.builder()
                .availableStock(10)
                .minimumStock(5)
                .product(product)
                .build();

        product.setInventory(inventory);
        Product saved = productRepository.save(product);

        assertNotNull(saved.getId());
        assertEquals("LAP-001", saved.getSku());
    }

    @Test
    void shouldFindProductsWithLowStock() {
        Product lowStockProduct = createProduct("SKU-LOW", "Bajo Stock", 3, 5);

        createProduct("SKU-OK", "Stock Normal", 10, 5);

        List<Product> lowStockList = productRepository.findProductsWithLowStock();

        assertFalse(lowStockList.isEmpty());
        assertEquals(1, lowStockList.size());
        assertEquals("SKU-LOW", lowStockList.get(0).getSku());
    }

    @Test
    void shouldFindProductsByCategory() {
        createProduct("SKU-1", "Prod 1", 10, 2);
        createProduct("SKU-2", "Prod 2", 10, 2);

        List<Product> products = productRepository.findByCategoryId(savedCategory.getId());

        assertEquals(2, products.size());
    }

    @Test
    void shouldOrderByPriceAsc() {
        createProduct("SKU-CARO", "Caro", 10, 2, new BigDecimal("500"));
        createProduct("SKU-BARATO", "Barato", 10, 2, new BigDecimal("100"));

        List<Product> ordered = productRepository.findAllByOrderByPriceAsc();

        assertEquals("SKU-BARATO", ordered.get(0).getSku());
    }


    private Product createProduct(String sku, String name, Integer available, Integer min) {
        return createProduct(sku, name, available, min, new BigDecimal("10.00"));
    }

    private Product createProduct(String sku, String name, Integer available, Integer min, BigDecimal price) {
        Product p = Product.builder()
                .sku(sku)
                .name(name)
                .description("Desc")
                .price(price)
                .active(true)
                .category(savedCategory)
                .build();

        Inventory i = Inventory.builder()
                .availableStock(available)
                .minimumStock(min)
                .product(p)
                .build();

        p.setInventory(i);
        return productRepository.save(p);
    }

    @Test
    void shouldFindProductBySku() {
        createProduct("SKU-001", "Producto 1", 10, 2);

        Optional<Product> found = productRepository.findBySku("SKU-001");

        assertTrue(found.isPresent());
        assertEquals("SKU-001", found.get().getSku());
    }

    @Test
    void shouldReturnEmptyWhenSkuNotFound() {
        Optional<Product> found = productRepository.findBySku("SKU-INEXISTENTE");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindProductByName() {
        createProduct("SKU-002", "Laptop Pro", 10, 2);

        Optional<Product> found = productRepository.findByName("Laptop Pro");

        assertTrue(found.isPresent());
        assertEquals("Laptop Pro", found.get().getName());
    }

    @Test
    void shouldFindActivProductsByCategory() {
        createProduct("SKU-ACT", "Activo", 10, 2);

        Product inactive = Product.builder()
                .sku("SKU-INACT")
                .name("Inactivo")
                .description("Desc")
                .price(new BigDecimal("10.00"))
                .active(false)
                .category(savedCategory)
                .build();
        productRepository.save(inactive);

        List<Product> actives = productRepository.findByCategoryAndActiveTrue(savedCategory);

        assertEquals(1, actives.size());
        assertEquals("SKU-ACT", actives.get(0).getSku());
    }

    @Test
    void shouldFindAllActiveProducts() {
        createProduct("SKU-A1", "Activo 1", 10, 2);
        createProduct("SKU-A2", "Activo 2", 10, 2);

        Product inactive = Product.builder()
                .sku("SKU-INACT")
                .name("Inactivo")
                .description("Desc")
                .price(new BigDecimal("10.00"))
                .active(false)
                .category(savedCategory)
                .build();
        productRepository.save(inactive);

        List<Product> actives = productRepository.findByActiveTrue();

        assertEquals(2, actives.size());
    }
}
