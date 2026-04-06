package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.CategoryDtos;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.service.mapper.CategoryMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {
    private final CategoryMapper mapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    void toEntity_shouldMapCreate() {
        Category entity = mapper.toEntity(new CategoryDtos.CategoryCreateRequest(
                "Tecnologia",
                "Productos de tecnologia"
        ));

        assertNull(entity.getId());
        assertEquals("Tecnologia", entity.getName());
        assertEquals("Productos de tecnologia", entity.getDescription());
        assertNotNull(entity.getProducts());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Category entity = Category.builder()
                .id(id)
                .name("Tecnologia")
                .description("Productos de tecnologia")
                .build();

        CategoryDtos.CategoryResponse dto = mapper.toResponse(entity);

        assertEquals(id, dto.id());
        assertEquals("Tecnologia", dto.name());
        assertEquals("Productos de tecnologia", dto.description());
    }

    @Test
    void patch_shouldIgnoreNulls() {
        Category target = Category.builder()
                .name("Old Name")
                .description("Old Desc")
                .build();

        mapper.patch(target, new CategoryDtos.CategoryUpdateRequest(
                null,
                "New Desc"
        ));

        assertEquals("Old Name", target.getName());
        assertEquals("New Desc", target.getDescription());
        assertNotNull(target.getProducts());
    }
}

