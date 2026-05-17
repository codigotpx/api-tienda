package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.ProductDtos.*;
import com.tienda.universitaria.api.domain.entities.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "imageUrl", source = "imageUrl")
    Product toEntity(ProductCreateRequest req);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "imageUrl", source = "imageUrl")
    ProductResponse toResponse(Product entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sku", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "inventory", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "imageUrl", source = "imageUrl")
    void patch(@MappingTarget Product target, ProductUpdateRequest changes);
}
