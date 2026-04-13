package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.InventoryDtos;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.InventoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    public InventoryDtos.InventoryResponse create(UUID productId, InventoryDtos.InventoryCreateRequest req) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("InventoryCreateRequest must not be null");
        }
        if (req.availableStock() < 0) {
            throw new IllegalArgumentException("availableStock must be >= 0");
        }
        if (req.minimumStock() < 0) {
            throw new IllegalArgumentException("minimumStock must be >= 0");
        }
        if (inventoryRepository.findByProductId(productId).isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for product: " + productId);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        Inventory inventory = inventoryMapper.toEntity(req);
        inventory.setProduct(product);

        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(saved);
    }

    @Override
    public InventoryDtos.InventoryResponse update(UUID productId, InventoryDtos.InventoryUpdateRequest req) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("InventoryUpdateRequest must not be null");
        }
        if (req.availableStock() < 0) {
            throw new IllegalArgumentException("availableStock must be >= 0");
        }
        if (req.minimumStock() < 0) {
            throw new IllegalArgumentException("minimumStock must be >= 0");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + productId));

        inventoryMapper.patch(inventory, req);
        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDtos.InventoryResponse get(UUID inventoryId) {
        if (inventoryId == null) {
            throw new IllegalArgumentException("inventoryId must not be null");
        }

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + inventoryId));
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDtos.InventoryResponse getByProduct(UUID productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + productId));
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDtos.InventoryResponse> getAll(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(inventoryMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryDtos.InventoryResponse> getLowStock(Pageable pageable) {
        return inventoryRepository.findLowStock(pageable).map(inventoryMapper::toResponse);
    }

    @Override
    public InventoryDtos.InventoryResponse adjustStock(UUID productId, int delta) {
        if (productId == null) {
            throw new IllegalArgumentException("productId must not be null");
        }

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + productId));

        int next = inventory.getAvailableStock() + delta;
        if (next < 0) {
            throw new IllegalArgumentException("availableStock can not become negative. productId=%s current=%s delta=%s"
                    .formatted(productId, inventory.getAvailableStock(), delta));
        }

        inventory.setAvailableStock(next);
        Inventory saved = inventoryRepository.save(inventory);
        return inventoryMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID inventoryId) {
        if (inventoryId == null) {
            throw new IllegalArgumentException("inventoryId must not be null");
        }

        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found: " + inventoryId));
        inventoryRepository.delete(inventory);
    }
}

