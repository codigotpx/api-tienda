package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProductId(UUID productId);

    @Query("SELECT i FROM Inventory i WHERE i.availableStock < i.minimumStock")
    Page<Inventory> findLowStock(Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventory i
        SET i.availableStock = i.availableStock - :qty
        WHERE i.product.id = :productId
          AND i.availableStock >= :qty
        """)
    int tryDecrementStock(@Param("productId") UUID productId, @Param("qty") int qty);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Inventory i
        SET i.availableStock = i.availableStock + :qty
        WHERE i.product.id = :productId
        """)
    int incrementStock(@Param("productId") UUID productId, @Param("qty") int qty);

    @Query("""

        SELECT p.id, p.name, p.sku, i.availableStock, i.minimumStock
        FROM Inventory i
        JOIN i.product p
        WHERE i.availableStock < i.minimumStock
        """)
    List<Object[]> findLowStockProductsRaw();



}
