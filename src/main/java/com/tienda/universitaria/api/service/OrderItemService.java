package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderItemDtos;

import java.util.List;
import java.util.UUID;

public interface OrderItemService {

    List<OrderItemDtos.OrderItemResponse> getByOrder(UUID orderId);

    OrderItemDtos.OrderItemResponse addToOrder(UUID orderId, OrderItemDtos.OrderItemCreateRequest req);

    OrderItemDtos.OrderItemResponse updateQuantity(UUID orderId, UUID orderItemId, int quantity);

    void delete(UUID orderId, UUID orderItemId);
}

