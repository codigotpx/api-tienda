package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.OrderItemMapper;
import com.tienda.universitaria.api.service.mapper.OrderMapper;
import com.tienda.universitaria.api.service.mapper.OrderStatusHistoryMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
            throw new IllegalArgumentException("OrderCreateRequest must not be null");
        }
        if (req.customerId() == null) {
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (req.addressId() == null) {
            throw new IllegalArgumentException("addressId must not be null");
        }
        if (req.orderItems() == null || req.orderItems().isEmpty()) {
            throw new IllegalArgumentException("orderItems must not be empty");
        }

        Customer customer = customerRepository.findById(req.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + req.customerId()));

        if (!addressRepository.existsByIdAndCustomerId(req.addressId(), req.customerId())) {
            throw new EntityNotFoundException("Address not found for customer. customerId=%s addressId=%s"
                    .formatted(req.customerId(), req.addressId()));
        }
        Address address = addressRepository.findById(req.addressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + req.addressId()));

        Order order = orderMapper.toEntity(req);
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemDtos.OrderItemCreateRequest itemReq : req.orderItems()) {
            if (itemReq == null) {
                throw new IllegalArgumentException("orderItems contains null item");
            }
            if (itemReq.productId() == null) {
                throw new IllegalArgumentException("productId must not be null");
            }
            if (itemReq.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be > 0");
            }

            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.productId()));
            if (Boolean.FALSE.equals(product.getActive())) {
                throw new IllegalArgumentException("Product is inactive: " + itemReq.productId());
            }

            Inventory inventory = inventoryRepository.findByProductId(itemReq.productId())
                    .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + itemReq.productId()));
            if (inventory.getAvailableStock() < itemReq.quantity()) {
                throw new IllegalArgumentException("Not enough stock for product %s. available=%s requested=%s"
                        .formatted(itemReq.productId(), inventory.getAvailableStock(), itemReq.quantity()));
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
            throw new IllegalArgumentException("id must not be null");
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + id));
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
            throw new IllegalArgumentException("customerId must not be null");
        }
        if (!customerRepository.existsById(customerId)) {
            throw new EntityNotFoundException("Customer not found: " + customerId);
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
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("newStatus must not be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        OrderStatus previous = order.getStatus();
        if (previous == OrderStatus.CANCELLED || previous == OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Order status can not be changed from " + previous);
        }
        if (previous == newStatus) {
            return orderMapper.toResponse(order);
        }

        if (!isValidTransition(previous, newStatus)) {
            throw new IllegalArgumentException("Invalid order status transition: " + previous + " -> " + newStatus);
        }

        // Apply inventory "discount" (reserve stock) at payment time, not at creation time.
        if (newStatus == OrderStatus.PAID) {
            for (OrderItem oi : order.getOrderItems()) {
                UUID productId = oi.getProduct().getId();
                int updated = inventoryRepository.tryDecrementStock(productId, oi.getQuantity());
                if (updated != 1) {
                    throw new IllegalArgumentException("Insufficient stock to pay order. productId=%s requested=%s"
                            .formatted(productId, oi.getQuantity()));
                }
            }
        }

        // Reverse stock only if it was previously reserved (paid).
        if (newStatus == OrderStatus.CANCELLED && previous == OrderStatus.PAID) {
            for (OrderItem oi : order.getOrderItems()) {
                UUID productId = oi.getProduct().getId();
                inventoryRepository.incrementStock(productId, oi.getQuantity());
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
            throw new IllegalArgumentException("orderId must not be null");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

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
}
