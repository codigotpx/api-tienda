package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.entities.*;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.OrderItemMapper;
import com.tienda.universitaria.api.service.mapper.OrderMapper;
import com.tienda.universitaria.api.service.mapper.OrderStatusHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.api.exception.BusinessException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusHistoryMapper orderStatusHistoryMapper;

    @Override
    public OrderDtos.OrderResponse create(OrderDtos.OrderCreateRequest req) {
        if (req == null) {
            throw new ValidationException("OrderCreateRequest must not be null");
        }
        if (req.customerId() == null) {
            throw new ValidationException("customerId must not be null");
        }
        if (req.addressId() == null) {
            throw new ValidationException("addressId must not be null");
        }
        if (req.orderItems() == null || req.orderItems().isEmpty()) {
            throw new ValidationException("orderItems must not be empty");
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + req.customerId()));
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("Customer status must be ACTIVE");
        }

        if (!addressRepository.existsByIdAndCustomerId(req.addressId(), req.customerId())) {
            throw new ResourceNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(req.customerId(), req.addressId()));
        }
        Address address = addressRepository.findById(req.addressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + req.addressId()));

        Order order = orderMapper.toEntity(req);
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDtos.OrderItemCreateRequest itemReq : req.orderItems()) {
            if (itemReq == null) {
                throw new ValidationException("orderItems contains null item");
            }
            if (itemReq.productId() == null) {
                throw new ValidationException("productId must not be null");
            }
            if (itemReq.quantity() <= 0) {
                throw new ValidationException("quantity must be > 0");
            }

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.productId()));
            if (Boolean.FALSE.equals(product.getActive())) {
                throw new BusinessException("Product is inactive: " + itemReq.productId());
            }

            BigDecimal unitPrice = product.getPrice();
            if (unitPrice == null) {
                throw new IllegalStateException("Product price is null: " + itemReq.productId());
            }

            BigDecimal subtotal = unitPrice
                    .multiply(BigDecimal.valueOf(itemReq.quantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            total = total.add(subtotal);

            OrderItem orderItem = orderItemMapper.toEntity(itemReq);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            order.getOrderItems().add(orderItem);
        }

        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));

        OrderStatusHistory createdHistory = OrderStatusHistory.builder()
                .order(order)
                .previousStatus(null)
                .newStatus(OrderStatus.CREATED)
                .notes("Order created")
                .build();
        order.getOrderStatusHistory().add(createdHistory);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDtos.OrderResponse get(UUID id) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> getAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> getByCustomer(UUID customerId) {
        if (customerId == null) {
            throw new ValidationException("customerId must not be null");
        }
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: " + customerId);
        }

        return orderRepository.findByCustomerId(customerId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDtos.OrderResponse> findByFilters(
            UUID customerId,
            OrderStatus status,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal minTotal,
            BigDecimal maxTotal
    ) {
        return orderRepository.findByFilters(customerId, status, from, to, minTotal, maxTotal).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    public OrderDtos.OrderResponse setStatus(UUID orderId, OrderStatus newStatus, String notes) {
        if (orderId == null) {
            throw new ValidationException("orderId must not be null");
        }
        if (newStatus == null) {
            throw new ValidationException("newStatus must not be null");
        }


        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        OrderStatus previous = order.getStatus();
        if (previous == newStatus) {
            return orderMapper.toResponse(order);
        }
        if (!isValidTransition(previous, newStatus)) {
            throw new BusinessException("Invalid order status transition: " + previous + " -> " + newStatus);
        }

        if (newStatus == OrderStatus.PAID) {
            Map<UUID, Integer> qtyByProduct = aggregateQtyByProduct(order);
            for (Map.Entry<UUID, Integer> e : qtyByProduct.entrySet()) {
                Inventory inv = inventoryRepository.findByProductId(e.getKey())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Inventory not found for product: " + e.getKey()));
                if (inv.getAvailableStock() < e.getValue()) {
                    throw new BusinessException(
                            "Insufficient stock to pay order. productId=%s available=%s requested=%s"
                                    .formatted(e.getKey(), inv.getAvailableStock(), e.getValue()));
                }
            }

            for (Map.Entry<UUID, Integer> e : qtyByProduct.entrySet()) {
                inventoryRepository.tryDecrementStock(e.getKey(), e.getValue());
            }
        }

        if (newStatus == OrderStatus.CANCELLED && previous == OrderStatus.PAID) {
            Map<UUID, Integer> qtyByProduct = aggregateQtyByProduct(order);
            for (Map.Entry<UUID, Integer> e : qtyByProduct.entrySet()) {
                inventoryRepository.incrementStock(e.getKey(), e.getValue());
            }
        }

        order.setStatus(newStatus);

        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .previousStatus(previous)
                .newStatus(newStatus)
                .notes(notes)
                .build();
        order.getOrderStatusHistory().add(history);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryDtos.OrderStatusHistoryResponse> getHistory(UUID orderId) {
        if (orderId == null) {
            throw new ValidationException("orderId must not be null");
        }

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

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        return switch (from) {
            case CREATED -> (to == OrderStatus.PAID || to == OrderStatus.CANCELLED);
            case PAID -> (to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED);
            case SHIPPED -> (to == OrderStatus.DELIVERED);
            case DELIVERED, CANCELLED -> false;
        };
    }

    private Map<UUID, Integer> aggregateQtyByProduct(Order order) {
        Map<UUID, Integer> qtyByProduct = new HashMap<>();
        for (OrderItem oi : order.getOrderItems()) {
            UUID productId = oi.getProduct().getId();
            qtyByProduct.merge(productId, oi.getQuantity(), Integer::sum);
        }
        return qtyByProduct;
    }
}

