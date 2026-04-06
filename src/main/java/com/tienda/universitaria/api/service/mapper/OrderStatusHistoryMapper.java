package com.tienda.universitaria.api.service.mapper;

import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos.*;
import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderStatusHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "previousStatus", ignore = true)
    @Mapping(target = "newStatus", ignore = true)
    @Mapping(target = "changedAt", ignore = true)
    @Mapping(target = "order", ignore = true)
    OrderStatusHistory toEntity(OrderStatusHistoryCreateRequest req);

    OrderStatusHistoryResponse toResponse(OrderStatusHistory entity);
}

