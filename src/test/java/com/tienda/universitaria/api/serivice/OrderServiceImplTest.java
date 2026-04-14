package com.tienda.universitaria.api.serivice;

import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.api.exception.BusinessException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.entities.Inventory;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.AddressRepository;
import com.tienda.universitaria.api.domain.repositories.CustomerRepository;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.OrderServiceImpl;
import com.tienda.universitaria.api.service.mapper.OrderItemMapper;
import com.tienda.universitaria.api.service.mapper.OrderMapper;
import com.tienda.universitaria.api.service.mapper.OrderStatusHistoryMapper;
import com.tienda.universitaria.api.domain.entities.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private AddressRepository addressRepository;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private OrderMapper orderMapper;
    @Mock private OrderItemMapper orderItemMapper;
    @Mock private OrderStatusHistoryMapper orderStatusHistoryMapper;

    @InjectMocks private OrderServiceImpl orderService;

    @Test
    void create_shouldRejectWhenNoItems() {
        var req = new OrderDtos.OrderCreateRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of()
        );

        assertThrows(ValidationException.class, () -> orderService.create(req));
        verifyNoInteractions(orderRepository, customerRepository, addressRepository, productRepository, inventoryRepository);
    }

    @Test
    void create_shouldRejectWhenQuantityInvalid() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        var req = new OrderDtos.OrderCreateRequest(
                customerId,
                addressId,
                List.of(new OrderItemDtos.OrderItemCreateRequest(productId, 0))
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(Customer.builder().id(customerId).status(CustomerStatus.ACTIVE).build()));
        when(addressRepository.existsByIdAndCustomerId(addressId, customerId)).thenReturn(true);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(Address.builder().id(addressId).build()));
        when(orderMapper.toEntity(req)).thenReturn(new Order());

        assertThrows(ValidationException.class, () -> orderService.create(req));
        verify(productRepository, never()).findById(any());
        verify(inventoryRepository, never()).tryDecrementStock(any(), anyInt());
        verify(inventoryRepository, never()).incrementStock(any(), anyInt());
    }

    @Test
    void create_shouldRejectWhenCustomerInactive() {
        UUID customerId = UUID.randomUUID();
        var req = new OrderDtos.OrderCreateRequest(
                customerId,
                UUID.randomUUID(),
                List.of(new OrderItemDtos.OrderItemCreateRequest(UUID.randomUUID(), 1))
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(Customer.builder().id(customerId).status(CustomerStatus.INACTIVE).build()));

        assertThrows(BusinessException.class, () -> orderService.create(req));
        verifyNoInteractions(addressRepository, productRepository, inventoryRepository, orderRepository);
    }

    @Test
    void create_shouldCalculateSubtotalsAndTotal_andNotDecrementInventory() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();

        var req = new OrderDtos.OrderCreateRequest(
                customerId,
                addressId,
                List.of(
                        new OrderItemDtos.OrderItemCreateRequest(productId1, 2),
                        new OrderItemDtos.OrderItemCreateRequest(productId2, 3)
                )
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(Customer.builder().id(customerId).status(CustomerStatus.ACTIVE).build()));
        when(addressRepository.existsByIdAndCustomerId(addressId, customerId)).thenReturn(true);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(Address.builder().id(addressId).build()));

        when(orderMapper.toEntity(req)).thenReturn(new Order());

        when(productRepository.findById(productId1))
                .thenReturn(Optional.of(Product.builder().id(productId1).active(true).price(new BigDecimal("10.00")).build()));
        when(productRepository.findById(productId2))
                .thenReturn(Optional.of(Product.builder().id(productId2).active(true).price(new BigDecimal("3.33")).build()));

        when(orderItemMapper.toEntity(any(OrderItemDtos.OrderItemCreateRequest.class)))
                .thenAnswer(inv -> OrderItem.builder().build());

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.create(req);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();

        assertEquals(OrderStatus.CREATED, saved.getStatus());
        assertEquals(new BigDecimal("29.99"), saved.getTotal()); // 2*10.00 + 3*3.33 = 29.99
        assertNotNull(saved.getOrderItems());
        assertEquals(2, saved.getOrderItems().size());
        assertEquals(
                Set.of(new BigDecimal("20.00"), new BigDecimal("9.99")),
                saved.getOrderItems().stream().map(OrderItem::getSubtotal).collect(java.util.stream.Collectors.toSet())
        );
        assertNotNull(saved.getOrderStatusHistory());
        assertEquals(1, saved.getOrderStatusHistory().size());

        verify(inventoryRepository, never()).tryDecrementStock(any(), anyInt());
        verify(inventoryRepository, never()).incrementStock(any(), anyInt());
    }

    @Test
    void create_shouldRejectWhenProductInactive() {
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        var req = new OrderDtos.OrderCreateRequest(
                customerId,
                addressId,
                List.of(new OrderItemDtos.OrderItemCreateRequest(productId, 1))
        );

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(Customer.builder().id(customerId).status(CustomerStatus.ACTIVE).build()));
        when(addressRepository.existsByIdAndCustomerId(addressId, customerId)).thenReturn(true);
        when(addressRepository.findById(addressId)).thenReturn(Optional.of(Address.builder().id(addressId).build()));
        when(orderMapper.toEntity(req)).thenReturn(new Order());

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(Product.builder().id(productId).active(false).price(new BigDecimal("10.00")).build()));

        assertThrows(BusinessException.class, () -> orderService.create(req));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void setStatus_shouldRejectInvalidTransition() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.CREATED).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class, () -> orderService.setStatus(orderId, OrderStatus.DELIVERED, null));
        verify(orderRepository, never()).save(any());
        verify(inventoryRepository, never()).tryDecrementStock(any(), anyInt());
    }

    @Test
    void setStatus_shouldRejectPaymentWhenInsufficientStock() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder().id(productId).build();
        OrderItem item = OrderItem.builder().product(product).quantity(5).unitPrice(new BigDecimal("1.00")).subtotal(new BigDecimal("5.00")).build();
        Order order = Order.builder().id(orderId).status(OrderStatus.CREATED).build();
        order.setOrderItems(Set.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(Inventory.builder().availableStock(2).build()));

        assertThrows(BusinessException.class, () -> orderService.setStatus(orderId, OrderStatus.PAID, "pay"));
        verify(inventoryRepository, never()).tryDecrementStock(any(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void setStatus_shouldDecrementInventoryWhenPaying() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder().id(productId).build();
        OrderItem item = OrderItem.builder().product(product).quantity(2).unitPrice(new BigDecimal("1.00")).subtotal(new BigDecimal("2.00")).build();
        Order order = Order.builder().id(orderId).status(OrderStatus.CREATED).build();
        order.setOrderItems(Set.of(item));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(inventoryRepository.findByProductId(productId)).thenReturn(Optional.of(Inventory.builder().availableStock(10).build()));
        when(inventoryRepository.tryDecrementStock(productId, 2)).thenReturn(1);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.setStatus(orderId, OrderStatus.PAID, "pay");

        assertEquals(OrderStatus.PAID, order.getStatus());
        verify(inventoryRepository).tryDecrementStock(productId, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void setStatus_shouldIncrementInventoryWhenCancellingPaidOrder_aggregatedByProduct() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = Product.builder().id(productId).build();
        OrderItem item1 = OrderItem.builder().product(product).quantity(2).build();
        OrderItem item2 = OrderItem.builder().product(product).quantity(3).build();
        Order order = Order.builder().id(orderId).status(OrderStatus.PAID).build();
        order.setOrderItems(Set.of(item1, item2));

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any(Order.class))).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.setStatus(orderId, OrderStatus.CANCELLED, "cancel");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(inventoryRepository).incrementStock(productId, 5);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void setStatus_shouldThrowWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.setStatus(orderId, OrderStatus.PAID, null));
    }

    @Test
    void setStatus_shouldCancelCreatedOrderWithoutRevertingStock() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.CREATED).build();
        order.setOrderItems(Set.of());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.setStatus(orderId, OrderStatus.CANCELLED, "cancelado");

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void setStatus_shouldTransitionFromPaidToShipped() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.PAID).build();
        order.setOrderItems(Set.of());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.setStatus(orderId, OrderStatus.SHIPPED, null);

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void setStatus_shouldTransitionFromShippedToDelivered() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.SHIPPED).build();
        order.setOrderItems(Set.of());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderMapper.toResponse(any())).thenReturn(mock(OrderDtos.OrderResponse.class));

        orderService.setStatus(orderId, OrderStatus.DELIVERED, null);

        assertEquals(OrderStatus.DELIVERED, order.getStatus());
        verifyNoInteractions(inventoryRepository);
    }

    @Test
    void setStatus_shouldRejectCancellingDeliveredOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder().id(orderId).status(OrderStatus.DELIVERED).build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.setStatus(orderId, OrderStatus.CANCELLED, null));
        verify(orderRepository, never()).save(any());
    }
}
