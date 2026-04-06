package com.tienda.universitaria.api.api.dto;

import com.tienda.universitaria.api.domain.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDtos {
    public record OrderCreateRequest(
            UUID customerId,
            UUID addressId,
            List<OrderItemDtos.OrderItemCreateRequest> orderItems
    ) {}

    public record OrderResponse(
            UUID id,
            BigDecimal total,
            OrderStatus status,
            UUID customerId,
            String customerName,
            UUID addressId,
            List<OrderItemDtos.OrderItemResponse> orderItems,
            LocalDateTime createAt,
            LocalDateTime updateAt
    ) {}
}
