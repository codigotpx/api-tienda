package com.tienda.universitaria.api.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemDtos {
    public record OrderItemCreateRequest(
            UUID productId,
            int quantity
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
