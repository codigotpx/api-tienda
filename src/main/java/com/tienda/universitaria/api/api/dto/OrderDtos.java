package com.tienda.universitaria.api.api.dto;

import com.tienda.universitaria.api.domain.enums.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class OrderDtos {

    public record OrderCreateRequest(
            @NotNull UUID customerId,
            @NotNull UUID addressId,
            @NotNull @NotEmpty(message = "Order must contain at least one item") @Valid
            List<OrderItemDtos.OrderItemCreateRequest> orderItems
    ) implements Serializable {}

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
    ) implements Serializable {}
}
