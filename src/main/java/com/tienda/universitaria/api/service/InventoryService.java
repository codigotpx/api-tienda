package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.InventoryDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface InventoryService {

    InventoryDtos.InventoryResponse create(UUID productId, InventoryDtos.InventoryCreateRequest req);

    InventoryDtos.InventoryResponse update(UUID productId, InventoryDtos.InventoryUpdateRequest req);

    InventoryDtos.InventoryResponse get(UUID inventoryId);

    InventoryDtos.InventoryResponse getByProduct(UUID productId);

    Page<InventoryDtos.InventoryResponse> getAll(Pageable pageable);

    Page<InventoryDtos.InventoryResponse> getLowStock(Pageable pageable);

    InventoryDtos.InventoryResponse adjustStock(UUID productId, int delta);

    void delete(UUID inventoryId);
}

