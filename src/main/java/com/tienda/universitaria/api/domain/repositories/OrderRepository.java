package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    List<Order> findByCustomerId(UUID customerId);

    // Use COALESCE to avoid generating "... (? IS NULL OR ...)" SQL fragments where
    // Postgres can't infer the type of a NULL bind parameter.
    @Query("""
        SELECT o
        FROM Order o
        WHERE o.customer.id = COALESCE(:customerId, o.customer.id)
          AND o.status = COALESCE(:status, o.status)
          AND o.createdAt >= COALESCE(:from, o.createdAt)
          AND o.createdAt <= COALESCE(:to, o.createdAt)
          AND o.total >= COALESCE(:minTotal, o.total)
          AND o.total <= COALESCE(:maxTotal, o.total)
        """)
    List<Order> findByFilters(
            @Param("customerId") UUID customerId,
            @Param("status") OrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal
    );

    @Query("SELECT YEAR(o.createdAt), MONTH(o.createdAt), SUM(o.total) " +
            "FROM Order o " +
            "WHERE o.status = 'DELIVERED' " +
            "GROUP BY YEAR(o.createdAt), MONTH(o.createdAt) " +
            "ORDER BY YEAR(o.createdAt) DESC, MONTH(o.createdAt) DESC")
    List<Object[]> findMonthlyIncomeRaw();

    @Query("""
        SELECT c.id, c.firstName, c.lastName, SUM(o.total)
        FROM Order o
        JOIN o.customer c
        WHERE o.status = 'DELIVERED'
        GROUP BY c.id, c.firstName, c.lastName
        ORDER BY SUM(o.total) DESC
        """)
    List<Object[]> findTopCustomersRaw();

    @Query("""
        SELECT c.name, SUM(oi.quantity)
        FROM OrderItem oi
        JOIN oi.product p
        JOIN p.category c
        JOIN oi.order o
        WHERE o.status != 'CANCELLED'
        GROUP BY c.name
        ORDER BY SUM(oi.quantity) DESC
        """)
    List<Object[]> findTopCategoriesByVolumeRaw();
}
