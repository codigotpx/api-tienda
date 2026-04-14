package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import com.tienda.universitaria.api.domain.repositories.OrderStatusHistoryRepository;
import com.tienda.universitaria.api.service.mapper.OrderStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderStatusHistoryServiceImpl implements OrderStatusHistoryService {
    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDtos.OrderStatusHistoryResponse> getByOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        return order.getOrderStatusHistory().stream()
                .sorted((a, b) -> {
                    if (a.getChangedAt() == null && b.getChangedAt() == null) return 0;
                    if (a.getChangedAt() == null) return -1;
                    if (b.getChangedAt() == null) return 1;
                    return a.getChangedAt().compareTo(b.getChangedAt());
                })
                .map(orderStatusHistoryMapper::toResponse)
                .toList();
    }

    @Override
    public OrderStatusHistoryDtos.OrderStatusHistoryResponse add(
            UUID orderId,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            OrderStatusHistoryDtos.OrderStatusHistoryCreateRequest req) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatusHistory history = orderStatusHistoryMapper.toEntity(req);
        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);

        return orderStatusHistoryMapper.toResponse(orderStatusHistoryRepository.save(history));
    }
}
