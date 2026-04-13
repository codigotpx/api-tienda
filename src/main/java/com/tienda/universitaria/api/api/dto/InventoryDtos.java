package com.tienda.universitaria.api.api.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.io.Serializable;
import java.util.UUID;

public class InventoryDtos {

    public record InventoryCreateRequest(
            @PositiveOrZero(message = "Available stock cannot be negative") int availableStock,
            @PositiveOrZero(message = "Minimum stock cannot be negative") int minimumStock
    ) implements Serializable {}

    public record InventoryUpdateRequest(
            @PositiveOrZero(message = "Available stock cannot be negative") int availableStock,
            @PositiveOrZero(message = "Minimum stock cannot be negative") int minimumStock
    ) implements Serializable {}

    public record InventoryResponse(
            UUID id,
            int availableStock,
            int minimumStock,
            UUID productId
    ) implements Serializable {}
}