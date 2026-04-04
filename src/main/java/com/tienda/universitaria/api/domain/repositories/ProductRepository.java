package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByName(String name);

    List<Product> findByCategoryAndActiveTrue(Category category);

    List<Product> findByCategoryId(UUID categoryId);

    List<Product> findByActiveTrue();

    List<Product> findAllByOrderByPriceAsc();

    @Query("SELECT p FROM Product p WHERE p.inventory.availableStock <= p.inventory.minimumStock")
    List<Product> findProductsWithLowStock();
}
