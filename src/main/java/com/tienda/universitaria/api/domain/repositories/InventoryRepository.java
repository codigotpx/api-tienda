package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId(UUID productId);

    @Query("""

        SELECT p.id, p.name, p.sku, i.availableStock, i.minimumStock
        FROM Inventory i
        JOIN i.product p
        WHERE i.availableStock < i.minimumStock
        """)
    List<Object[]> findLowStockProductsRaw();



}
