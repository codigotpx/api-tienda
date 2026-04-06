package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.OrderDtos.*;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.entities.Order;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "orderStatusHistory", ignore = true)
    Order toEntity(OrderCreateRequest req);

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", expression = "java(fullName(entity.getCustomer()))")
    @Mapping(target = "addressId", source = "address.id")
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "createAt", source = "createdAt")
    @Mapping(target = "updateAt", source = "updatedAt")
    OrderResponse toResponse(Order entity);

    default String fullName(Customer customer) {
        if (customer == null) return null;
        String first = customer.getFirstName();
        String last = customer.getLastName();
        if (first == null || first.isBlank()) return last;
        if (last == null || last.isBlank()) return first;
        return first + " " + last;
    }
}
