package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.AddressDtos.*;
import com.tienda.universitaria.api.domain.entities.Address;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Address toEntity(AddressCreateRequest req);

    @Mapping(target = "customerId", source = "customer.id")
    AddressResponse toResponse(Address entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    void patch(@MappingTarget Address target, AddressUpdateRequest changes);

}
