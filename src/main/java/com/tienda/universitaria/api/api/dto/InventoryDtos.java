package com.tienda.universitaria.api.api.dto;

import java.util.UUID;

public class InventoryDtos {
    public record InventoryCreateRequest(
            int availableStock,
            int minimumStock
    ) {}

    public record InventoryUpdateRequest(
            int availableStock,
            int minimumStock
    ) {}

    public record InventoryResponse(
            UUID id,
            int availableStock,
            int minimumStock,
            UUID productId
    ) {}
}
