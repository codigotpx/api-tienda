package com.tienda.universitaria.api.serivice;

import com.tienda.universitaria.api.api.dto.InventoryDtos;
import com.tienda.universitaria.api.api.exception.BusinessException;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.InventoryServiceImpl;
import com.tienda.universitaria.api.service.mapper.InventoryMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.tienda.universitaria.api.api.exception.ValidationException;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryMapper inventoryMapper;

    @InjectMocks private InventoryServiceImpl inventoryService;

    @Test
    void create_shouldCreateInventoryForProduct() {
        UUID productId = UUID.randomUUID();
        var req = new InventoryDtos.InventoryCreateRequest(10, 2);

        var product = Product.builder().id(productId).build();
        var entity = Inventory.builder().availableStock(10).minimumStock(2).build();
        var saved = Inventory.builder().id(UUID.randomUUID()).availableStock(10).minimumStock(2).product(product).build();
        var expected = new InventoryDtos.InventoryResponse(saved.getId(), 10, 2, productId);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(inventoryMapper.toEntity(req)).thenReturn(entity);
        when(inventoryRepository.save(entity)).thenReturn(saved);
        when(inventoryMapper.toResponse(saved)).thenReturn(expected);

        var actual = inventoryService.create(productId, req);

        assertEquals(expected, actual);
        assertSame(product, entity.getProduct());
        verify(inventoryRepository).findByProductId(productId);
        verify(productRepository).findById(productId);
        verify(inventoryMapper).toEntity(req);
        verify(inventoryRepository).save(entity);
        verify(inventoryMapper).toResponse(saved);
    }

    @Test
    void create_shouldRejectWhenInventoryAlreadyExistsForProduct() {
        UUID productId = UUID.randomUUID();
        var req = new InventoryDtos.InventoryCreateRequest(10, 2);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(Inventory.builder().build()));

        assertThrows(ConflictException.class, () -> inventoryService.create(productId, req));
        verify(inventoryRepository).findByProductId(productId);
        verifyNoInteractions(productRepository, inventoryMapper);
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectNegativeStocks() {
        UUID productId = UUID.randomUUID();

        assertThrows(ValidationException.class,
                () -> inventoryService.create(productId, new InventoryDtos.InventoryCreateRequest(-1, 1)));
        assertThrows(ValidationException.class,
                () -> inventoryService.create(productId, new InventoryDtos.InventoryCreateRequest(1, -1)));
    }

    @Test
    void update_shouldPatchAndSaveInventory() {
        UUID productId = UUID.randomUUID();
        var req = new InventoryDtos.InventoryUpdateRequest(7, 3);
        var product = Product.builder().id(productId).build();
        var existing = Inventory.builder().id(UUID.randomUUID()).availableStock(5).minimumStock(2).product(product).build();
        var saved = Inventory.builder().id(existing.getId()).availableStock(7).minimumStock(3).product(product).build();
        var expected = new InventoryDtos.InventoryResponse(saved.getId(), 7, 3, productId);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(existing));
        when(inventoryRepository.save(existing)).thenReturn(saved);
        when(inventoryMapper.toResponse(saved)).thenReturn(expected);

        var actual = inventoryService.update(productId, req);

        assertEquals(expected, actual);
        verify(inventoryRepository).findByProductId(productId);
        verify(inventoryMapper).patch(eq(existing), eq(req));
        verify(inventoryRepository).save(existing);
        verify(inventoryMapper).toResponse(saved);
    }

    @Test
    void update_shouldThrowWhenInventoryNotFoundForProduct() {
        UUID productId = UUID.randomUUID();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.update(productId, new InventoryDtos.InventoryUpdateRequest(1, 1)));
    }

    @Test
    void get_shouldReturnInventoryResponse() {
        UUID inventoryId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        var product = Product.builder().id(productId).build();
        var inv = Inventory.builder().id(inventoryId).availableStock(10).minimumStock(2).product(product).build();
        var expected = new InventoryDtos.InventoryResponse(inventoryId, 10, 2, productId);

        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inv));
        when(inventoryMapper.toResponse(inv)).thenReturn(expected);

        var actual = inventoryService.get(inventoryId);

        assertEquals(expected, actual);
        verify(inventoryRepository).findById(inventoryId);
        verify(inventoryMapper).toResponse(inv);
    }

    @Test
    void getByProduct_shouldThrowWhenMissing() {
        UUID productId = UUID.randomUUID();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getByProduct(productId));
    }

    @Test
    void getAll_shouldMapAllInventories() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        var i1 = Inventory.builder().id(UUID.randomUUID()).availableStock(10).minimumStock(2).product(Product.builder().id(p1).build()).build();
        var i2 = Inventory.builder().id(UUID.randomUUID()).availableStock(1).minimumStock(5).product(Product.builder().id(p2).build()).build();

        var pageable = PageRequest.of(0, 10);
        when(inventoryRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(i1, i2), pageable, 2));
        when(inventoryMapper.toResponse(i1)).thenReturn(new InventoryDtos.InventoryResponse(i1.getId(), 10, 2, p1));
        when(inventoryMapper.toResponse(i2)).thenReturn(new InventoryDtos.InventoryResponse(i2.getId(), 1, 5, p2));

        var result = inventoryService.getAll(pageable);

        assertEquals(2, result.getContent().size());
        verify(inventoryRepository).findAll(pageable);
        verify(inventoryMapper).toResponse(i1);
        verify(inventoryMapper).toResponse(i2);
    }

    @Test
    void getLowStock_shouldReturnOnlyInventoriesBelowMinimum() {
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        var ok = Inventory.builder().id(UUID.randomUUID()).availableStock(10).minimumStock(2).product(Product.builder().id(p1).build()).build();
        var low = Inventory.builder().id(UUID.randomUUID()).availableStock(1).minimumStock(5).product(Product.builder().id(p2).build()).build();

        var pageable = PageRequest.of(0, 10);
        when(inventoryRepository.findLowStock(pageable)).thenReturn(new PageImpl<>(List.of(low), pageable, 1));
        when(inventoryMapper.toResponse(low)).thenReturn(new InventoryDtos.InventoryResponse(low.getId(), 1, 5, p2));

        var result = inventoryService.getLowStock(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(low.getId(), result.getContent().getFirst().id());
        verify(inventoryRepository).findLowStock(pageable);
        verify(inventoryMapper, never()).toResponse(ok);
        verify(inventoryMapper).toResponse(low);
    }

    @Test
    void adjustStock_shouldUpdateStockAndSave() {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();
        var product = Product.builder().id(productId).build();
        var inv = Inventory.builder().id(inventoryId).availableStock(10).minimumStock(2).product(product).build();
        var saved = Inventory.builder().id(inventoryId).availableStock(7).minimumStock(2).product(product).build();
        var expected = new InventoryDtos.InventoryResponse(inventoryId, 7, 2, productId);

        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inv));
        when(inventoryRepository.save(inv)).thenReturn(saved);
        when(inventoryMapper.toResponse(saved)).thenReturn(expected);

        var actual = inventoryService.adjustStock(productId, -3);

        assertEquals(expected, actual);
        assertEquals(7, inv.getAvailableStock());
        verify(inventoryRepository).save(inv);
    }

    @Test
    void adjustStock_shouldRejectWhenWouldBecomeNegative() {
        UUID productId = UUID.randomUUID();
        var inv = Inventory.builder().availableStock(2).minimumStock(0).product(Product.builder().id(productId).build()).build();
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(inv));

        assertThrows(BusinessException.class, () -> inventoryService.adjustStock(productId, -3));
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteInventory() {
        UUID inventoryId = UUID.randomUUID();
        var inv = Inventory.builder().id(inventoryId).build();
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.of(inv));

        inventoryService.delete(inventoryId);

        verify(inventoryRepository).findById(inventoryId);
        verify(inventoryRepository).delete(inv);
    }

    @Test
    void delete_shouldThrowWhenInventoryNotFound() {
        UUID inventoryId = UUID.randomUUID();
        when(inventoryRepository.findById(inventoryId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.delete(inventoryId));
        verify(inventoryRepository, never()).delete(any());
    }
}

