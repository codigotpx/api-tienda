package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.enums.OrderStatus;

import java.util.List;
import java.util.UUID;

public interface OrderStatusHistoryService {

    List<OrderStatusHistoryDtos.OrderStatusHistoryResponse> getByOrder(UUID orderId);

    OrderStatusHistoryDtos.OrderStatusHistoryResponse add(
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            OrderStatusHistoryDtos.OrderStatusHistoryCreateRequest req
    );
}

