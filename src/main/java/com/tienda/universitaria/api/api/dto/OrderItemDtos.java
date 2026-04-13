package com.tienda.universitaria.api.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemDtos {

    public record OrderItemCreateRequest(
            @NotNull UUID productId,
            @Min(value = 1, message = "Quantity must be greater than zero") int quantity
    ) implements Serializable {}

    public record OrderItemResponse(
            UUID id,
            UUID productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) implements Serializable {}
}
