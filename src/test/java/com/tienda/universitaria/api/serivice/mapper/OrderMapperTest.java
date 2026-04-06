package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.entities.Order;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.service.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderMapperTest {
    @Autowired
    private OrderMapper mapper;

    @Test
    void toEntity_shouldMapCreate_andIgnoreRelations() {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID addressId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");

        Order entity = mapper.toEntity(new OrderDtos.OrderCreateRequest(
                new BigDecimal("30.00"),
                customerId,
                addressId,
                List.of(new OrderItemDtos.OrderItemCreateRequest(productId, 3))
        ));

        assertNull(entity.getId());
        assertEquals(new BigDecimal("30.00"), entity.getTotal());
        assertEquals(OrderStatus.CREATED, entity.getStatus());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getCustomer());
        assertNull(entity.getAddress());
        assertNotNull(entity.getOrderItems());
        assertTrue(entity.getOrderItems().isEmpty());
        assertNotNull(entity.getOrderStatusHistory());
        assertTrue(entity.getOrderStatusHistory().isEmpty());
    }

    @Test
    void toResponse_shouldMapEntity_includingCustomerNameAndOrderItems() {
        UUID orderId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID addressId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID orderItemId = UUID.fromString("44444444-4444-4444-4444-444444444444");

        Customer customer = Customer.builder()
                .id(customerId)
                .firstName("Camilo")
                .lastName("Cerpa")
                .build();

        Address address = Address.builder()
                .id(addressId)
                .build();

        Product product = Product.builder()
                .id(productId)
                .name("Laptop")
                .build();

        Order order = Order.builder()
                .id(orderId)
                .total(new BigDecimal("30.00"))
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.of(2026, 4, 6, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 4, 6, 10, 5, 0))
                .customer(customer)
                .address(address)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(orderItemId)
                .quantity(3)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("30.00"))
                .order(order)
                .product(product)
                .build();

        order.setOrderItems(Set.of(orderItem));

        OrderDtos.OrderResponse dto = mapper.toResponse(order);

        assertEquals(orderId, dto.id());
        assertEquals(new BigDecimal("30.00"), dto.total());
        assertEquals(OrderStatus.PAID, dto.status());
        assertEquals(customerId, dto.customerId());
        assertEquals("Camilo Cerpa", dto.customerName());
        assertEquals(addressId, dto.addressId());
        assertEquals(LocalDateTime.of(2026, 4, 6, 10, 0, 0), dto.createAt());
        assertEquals(LocalDateTime.of(2026, 4, 6, 10, 5, 0), dto.updateAt());

        assertNotNull(dto.orderItems());
        assertEquals(1, dto.orderItems().size());
        OrderItemDtos.OrderItemResponse itemDto = dto.orderItems().getFirst();
        assertEquals(orderItemId, itemDto.id());
        assertEquals(productId, itemDto.productId());
        assertEquals("Laptop", itemDto.productName());
        assertEquals(3, itemDto.quantity());
        assertEquals(new BigDecimal("10.00"), itemDto.unitPrice());
        assertEquals(new BigDecimal("30.00"), itemDto.subtotal());
    }
}
