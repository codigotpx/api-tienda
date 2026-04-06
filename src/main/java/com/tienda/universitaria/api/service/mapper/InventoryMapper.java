package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.InventoryDtos.*;
import com.tienda.universitaria.api.domain.entities.Inventory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    Inventory toEntity(InventoryCreateRequest req);

    @Mapping(target = "productId", source = "product.id")
    InventoryResponse toResponse(Inventory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "product", ignore = true)
    void patch(@MappingTarget Inventory target, InventoryUpdateRequest changes);
}

