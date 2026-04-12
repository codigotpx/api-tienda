package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    Optional<Product> findByName(String name);

    List<Product> findByCategoryAndActiveTrue(Category category);

    List<Product> findByCategoryId(UUID categoryId);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByCategoryId(UUID categoryId, Pageable pageable);

    List<Product> findAllByOrderByPriceAsc();

    @Query("SELECT p FROM Product p WHERE p.inventory.availableStock <= p.inventory.minimumStock")
    List<Product> findProductsWithLowStock();

    @Query("SELECT p FROM Product p WHERE p.inventory.availableStock <= p.inventory.minimumStock")
    Page<Product> findProductsWithLowStock(Pageable pageable);
}
