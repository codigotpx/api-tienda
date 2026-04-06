package com.tienda.universitaria.api.api.dto;

import com.tienda.universitaria.api.domain.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrderStatusHistoryDtos {
    public record OrderStatusHistoryCreateRequest(
            String notes
    ) {}

    public record OrderStatusHistoryResponse(
            UUID id,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String notes,
            LocalDateTime changedAt
    ) {}
}
