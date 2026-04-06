package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.ProductDtos;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.service.mapper.ProductMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {
    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void toEntity_shouldMapCreate() {
        UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Product entity = mapper.toEntity(new ProductDtos.ProductCreateRequest(
                "SKU-001",
                "Laptop",
                "Laptop gamer",
                new BigDecimal("1234.56"),
                categoryId,
                true
        ));

        assertNull(entity.getId());
        assertNull(entity.getCategory());
        assertNull(entity.getInventory());
        assertNotNull(entity.getOrderItems());

        assertEquals("SKU-001", entity.getSku());
        assertEquals("Laptop", entity.getName());
        assertEquals("Laptop gamer", entity.getDescription());
        assertEquals(new BigDecimal("1234.56"), entity.getPrice());
        assertTrue(entity.getActive());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID productId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Category category = Category.builder()
                .id(categoryId)
                .name("Tecnologia")
                .description("Cat desc")
                .build();

        Product product = Product.builder()
                .id(productId)
                .sku("SKU-001")
                .name("Laptop")
                .description("Laptop gamer")
                .price(new BigDecimal("1234.56"))
                .active(true)
                .category(category)
                .build();

        ProductDtos.ProductResponse dto = mapper.toResponse(product);

        assertEquals(productId, dto.id());
        assertEquals("SKU-001", dto.sku());
        assertEquals("Laptop", dto.name());
        assertEquals("Laptop gamer", dto.description());
        assertEquals(new BigDecimal("1234.56"), dto.price());
        assertTrue(dto.active());
        assertEquals(categoryId, dto.categoryId());
        assertEquals("Tecnologia", dto.categoryName());
    }

    @Test
    void patch_shouldIgnoreNulls_andNotOverwriteSku() {
        UUID productId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID categoryId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Category category = Category.builder()
                .id(categoryId)
                .name("Tecnologia")
                .description("Cat desc")
                .build();

        Product target = Product.builder()
                .id(productId)
                .sku("SKU-OLD")
                .name("Old Name")
                .description("Old Desc")
                .price(new BigDecimal("10.00"))
                .active(true)
                .category(category)
                .build();

        mapper.patch(target, new ProductDtos.ProductUpdateRequest(
                "SKU-NEW",
                null,
                "New Desc",
                null,
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                false
        ));

        assertEquals(productId, target.getId());
        assertEquals("SKU-OLD", target.getSku());
        assertEquals("Old Name", target.getName());
        assertEquals("New Desc", target.getDescription());
        assertEquals(new BigDecimal("10.00"), target.getPrice());
        assertFalse(target.getActive());
        assertSame(category, target.getCategory());
    }
}

