package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.CategoryDtos.*;
import com.tienda.universitaria.api.domain.entities.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Category toEntity(CategoryCreateRequest req);

    CategoryResponse toResponse(Category entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void patch(@MappingTarget Category target, CategoryUpdateRequest changes);
}
