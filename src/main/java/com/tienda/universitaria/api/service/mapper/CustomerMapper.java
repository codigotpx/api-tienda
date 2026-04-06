package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.CustomerDtos.*;
import com.tienda.universitaria.api.domain.entities.Customer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Customer toEntity(CustomerCreateRequest req);

    CustomerResponse toResponse(Customer entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "orders", ignore = true)
    void patch(@MappingTarget Customer target , CustomerUpdateRequest changes);


}
