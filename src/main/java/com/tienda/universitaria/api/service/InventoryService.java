package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.InventoryDtos;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryDtos.InventoryResponse create(UUID productId, InventoryDtos.InventoryCreateRequest req);

    InventoryDtos.InventoryResponse update(UUID productId, InventoryDtos.InventoryUpdateRequest req);

    InventoryDtos.InventoryResponse get(UUID inventoryId);

    InventoryDtos.InventoryResponse getByProduct(UUID productId);

    List<InventoryDtos.InventoryResponse> getAll();

    List<InventoryDtos.InventoryResponse> getLowStock();

    InventoryDtos.InventoryResponse adjustStock(UUID productId, int delta);

    void delete(UUID inventoryId);
}

