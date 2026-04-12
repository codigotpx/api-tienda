package com.tienda.universitaria.api.api.dto;

import java.io.Serializable;
import java.util.UUID;

public class InventoryDtos {
    public record InventoryCreateRequest(
            int availableStock,
            int minimumStock
    ) implements Serializable {}

    public record InventoryUpdateRequest(
            int availableStock,
            int minimumStock
    ) implements Serializable {}

    public record InventoryResponse(
            UUID id,
            int availableStock,
            int minimumStock,
            UUID productId
    ) implements Serializable {}
}
