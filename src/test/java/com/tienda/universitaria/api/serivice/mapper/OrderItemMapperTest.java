package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.domain.entities.OrderItem;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.service.mapper.OrderItemMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemMapperTest {
    private final OrderItemMapper mapper = Mappers.getMapper(OrderItemMapper.class);

    @Test
    void toEntity_shouldMapCreate_andIgnoreCalculatedFields() {
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        OrderItem entity = mapper.toEntity(new OrderItemDtos.OrderItemCreateRequest(productId, 3));

        assertNull(entity.getId());
        assertEquals(3, entity.getQuantity());
        assertNull(entity.getUnitPrice());
        assertNull(entity.getSubtotal());
        assertNull(entity.getOrder());
        assertNull(entity.getProduct());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID orderItemId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID productId = UUID.fromString("11111111-1111-1111-1111-111111111111");

        Product product = Product.builder()
                .id(productId)
                .name("Laptop")
                .build();

        OrderItem entity = OrderItem.builder()
                .id(orderItemId)
                .quantity(3)
                .unitPrice(new BigDecimal("10.00"))
                .subtotal(new BigDecimal("30.00"))
                .product(product)
                .build();

        OrderItemDtos.OrderItemResponse dto = mapper.toResponse(entity);

        assertEquals(orderItemId, dto.id());
        assertEquals(productId, dto.productId());
        assertEquals("Laptop", dto.productName());
        assertEquals(3, dto.quantity());
        assertEquals(new BigDecimal("10.00"), dto.unitPrice());
        assertEquals(new BigDecimal("30.00"), dto.subtotal());
    }
}

