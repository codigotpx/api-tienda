package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderItemsRepository;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.OrderItemMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderItemServiceImpl implements OrderItemService {
    private final OrderItemsRepository orderItemsRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderItemMapper orderItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemDtos.OrderItemResponse> getByOrder(UUID orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Order not found: " + orderId);
        }

        return orderItemsRepository.findByOrderId(orderId).stream()
                .map(orderItemMapper::toResponse)
                .toList();
    }

    @Override
    public OrderItemDtos.OrderItemResponse addToOrder(UUID orderId, OrderItemDtos.OrderItemCreateRequest req) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("OrderItemCreateRequest must not be null");
        }
        if (req.productId() == null) {
            throw new IllegalArgumentException("productId must not be null");
        }
        if (req.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        ensureOrderIsEditable(order);

        Product product = productRepository.findById(req.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + req.productId()));
        if (Boolean.FALSE.equals(product.getActive())) {
            throw new IllegalArgumentException("Product is inactive: " + req.productId());
        }

        Inventory inventory = inventoryRepository.findByProductId(req.productId())
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + req.productId()));
        if (inventory.getAvailableStock() < req.quantity()) {
            throw new IllegalArgumentException("Not enough stock for product %s. available=%s requested=%s"
                    .formatted(req.productId(), inventory.getAvailableStock(), req.quantity()));
        }

        BigDecimal unitPrice = product.getPrice();
        if (unitPrice == null) {
            throw new IllegalStateException("Product price is null: " + req.productId());
        }

        OrderItem item = orderItemMapper.toEntity(req);
        item.setOrder(order);
        item.setProduct(product);
        item.setUnitPrice(unitPrice);
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(req.quantity())).setScale(2, RoundingMode.HALF_UP));

        OrderItem saved = orderItemsRepository.save(item);

        order.getOrderItems().add(saved);

        recalcAndPersistOrderTotal(order);

        return orderItemMapper.toResponse(saved);
    }

    @Override
    public OrderItemDtos.OrderItemResponse updateQuantity(UUID orderId, UUID orderItemId, int quantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (orderItemId == null) {
            throw new IllegalArgumentException("orderItemId must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }

        OrderItem item = orderItemsRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found: " + orderItemId));
        if (item.getOrder() == null || item.getOrder().getId() == null || !item.getOrder().getId().equals(orderId)) {
            throw new EntityNotFoundException("OrderItem not found for order. orderId=%s orderItemId=%s"
                    .formatted(orderId, orderItemId));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        ensureOrderIsEditable(order);

        UUID productId = item.getProduct().getId();
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found for product: " + productId));
        if (inventory.getAvailableStock() < quantity) {
            throw new IllegalArgumentException("Not enough stock for product %s. available=%s requested=%s"
                    .formatted(productId, inventory.getAvailableStock(), quantity));
        }

        item.setQuantity(quantity);
        BigDecimal unitPrice = item.getUnitPrice();
        if (unitPrice == null) {
            throw new IllegalStateException("OrderItem unitPrice is null: " + orderItemId);
        }
        item.setSubtotal(unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));

        OrderItem saved = orderItemsRepository.save(item);
        recalcAndPersistOrderTotal(order);
        return orderItemMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID orderId, UUID orderItemId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null");
        }
        if (orderItemId == null) {
            throw new IllegalArgumentException("orderItemId must not be null");
        }

        OrderItem item = orderItemsRepository.findById(orderItemId)
                .orElseThrow(() -> new EntityNotFoundException("OrderItem not found: " + orderItemId));
        if (item.getOrder() == null || item.getOrder().getId() == null || !item.getOrder().getId().equals(orderId)) {
            throw new EntityNotFoundException("OrderItem not found for order. orderId=%s orderItemId=%s"
                    .formatted(orderId, orderItemId));
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        ensureOrderIsEditable(order);

        orderItemsRepository.delete(item);
        order.getOrderItems().removeIf(oi -> orderItemId.equals(oi.getId()));

        recalcAndPersistOrderTotal(order);
    }

    private void ensureOrderIsEditable(Order order) {
        OrderStatus status = order.getStatus();
        // Items can only be modified before payment.
        if (status != OrderStatus.CREATED) {
            throw new IllegalArgumentException("Order is not editable in status: " + status);
        }
    }

    private void recalcAndPersistOrderTotal(Order order) {
        BigDecimal total = orderItemsRepository.findByOrderId(order.getId()).stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotal(total.setScale(2, RoundingMode.HALF_UP));
        orderRepository.save(order);
    }
}
