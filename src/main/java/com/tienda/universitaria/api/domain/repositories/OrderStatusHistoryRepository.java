package com.tienda.universitaria.api.domain.repositories;

import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {

    @Query("""
           SELECT h.previousStatus, h.newStatus, h.notes, h.changedAt
           FROM OrderStatusHistory h
           WHERE h.order.id = :orderId
           ORDER BY h.changedAt ASC
           """)
    List<Object[]> findHistoryByOrderIdRaw(@Param("orderId") UUID orderId);
}
