package com.tienda.universitaria.api.repository;


import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryTest {
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
    private CategoryRepository categoryRepository;

    private Category createCategory(String name, String description) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .build();
        return categoryRepository.save(category);
    }

    @Test
    void shouldSaveCategory() {
        Category saved = createCategory("Electrónica", "Gadgets y más");

        assertNotNull(saved.getId());
        assertEquals("Electrónica", saved.getName());
        assertEquals("Gadgets y más", saved.getDescription());
    }

    @Test
    void shouldFindCategoryByName() {
        createCategory("Libros", "Todo tipo de libros");

        Optional<Category> found = categoryRepository.findByName("Libros");

        assertTrue(found.isPresent());
        assertEquals("Libros", found.get().getName());
    }

    @Test
    void shouldReturnTrueWhenNameExists() {
        createCategory("Ropa", "Ropa universitaria");

        boolean exists = categoryRepository.existsByName("Ropa");

        assertTrue(exists);
    }

}
