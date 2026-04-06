package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    OrderDtos.OrderResponse create(OrderDtos.OrderCreateRequest req);

    OrderDtos.OrderResponse get(UUID id);

    List<OrderDtos.OrderResponse> getAll();

    List<OrderDtos.OrderResponse> getByCustomer(UUID customerId);

    List<OrderDtos.OrderResponse> findByFilters(
            UUID customerId,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal minTotal,
            BigDecimal maxTotal
    );

    OrderDtos.OrderResponse setStatus(UUID orderId, OrderStatus newStatus, String notes);

    List<OrderStatusHistoryDtos.OrderStatusHistoryResponse> getHistory(UUID orderId);
}

