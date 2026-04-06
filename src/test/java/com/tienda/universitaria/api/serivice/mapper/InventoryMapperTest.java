package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.InventoryDtos;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.service.mapper.InventoryMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InventoryMapperTest {
    private final InventoryMapper mapper = Mappers.getMapper(InventoryMapper.class);

    @Test
    void toEntity_shouldMapCreate() {
        Inventory entity = mapper.toEntity(new InventoryDtos.InventoryCreateRequest(10, 2));

        assertNull(entity.getId());
        assertNull(entity.getProduct());
        assertEquals(10, entity.getAvailableStock());
        assertEquals(2, entity.getMinimumStock());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID inventoryId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Product product = Product.builder().id(productId).build();
        Inventory entity = Inventory.builder()
                .id(inventoryId)
                .availableStock(10)
                .minimumStock(2)
                .product(product)
                .build();

        InventoryDtos.InventoryResponse dto = mapper.toResponse(entity);

        assertEquals(inventoryId, dto.id());
        assertEquals(10, dto.availableStock());
        assertEquals(2, dto.minimumStock());
        assertEquals(productId, dto.productId());
    }

    @Test
    void patch_shouldUpdatePrimitiveFields_andKeepProduct() {
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Product product = Product.builder().id(productId).build();

        Inventory target = Inventory.builder()
                .availableStock(10)
                .minimumStock(2)
                .product(product)
                .build();

        mapper.patch(target, new InventoryDtos.InventoryUpdateRequest(99, 7));

        assertEquals(99, target.getAvailableStock());
        assertEquals(7, target.getMinimumStock());
        assertSame(product, target.getProduct());
    }
}

