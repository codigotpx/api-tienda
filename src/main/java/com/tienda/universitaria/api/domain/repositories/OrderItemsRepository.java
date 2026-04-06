package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderItemsRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    boolean existsByProductIdAndOrderStatusNot(UUID productId, OrderStatus status);


    @Query("SELECT oi.product, SUM(oi.quantity) AS totalSold " +
            "FROM OrderItem oi " +
            "WHERE oi.order.createdAt BETWEEN :from AND :to " +
            "AND oi.order.status = 'DELIVERED' " +
            "GROUP BY oi.product " +
            "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findBestSellingProducts(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    boolean existsByProductIdAndOrderStatusIn(UUID productId, List<OrderStatus> statuses);

}
