package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.entities.OrderStatusHistory;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.service.mapper.OrderStatusHistoryMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusHistoryMapperTest {
    private final OrderStatusHistoryMapper mapper = Mappers.getMapper(OrderStatusHistoryMapper.class);

    @Test
    void toEntity_shouldMapCreate_andIgnoreSystemFields() {
        OrderStatusHistory entity = mapper.toEntity(new OrderStatusHistoryDtos.OrderStatusHistoryCreateRequest(
                "Paid by card"
        ));

        assertNull(entity.getId());
        assertEquals("Paid by card", entity.getNotes());
        assertNull(entity.getPreviousStatus());
        assertNull(entity.getNewStatus());
        assertNull(entity.getChangedAt());
        assertNull(entity.getOrder());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        LocalDateTime changedAt = LocalDateTime.of(2026, 4, 6, 10, 0, 0);

        OrderStatusHistory entity = OrderStatusHistory.builder()
                .id(id)
                .previousStatus(OrderStatus.CREATED)
                .newStatus(OrderStatus.PAID)
                .notes("Paid by card")
                .changedAt(changedAt)
                .build();

        OrderStatusHistoryDtos.OrderStatusHistoryResponse dto = mapper.toResponse(entity);

        assertEquals(id, dto.id());
        assertEquals(OrderStatus.CREATED, dto.previousStatus());
        assertEquals(OrderStatus.PAID, dto.newStatus());
        assertEquals("Paid by card", dto.notes());
        assertEquals(changedAt, dto.changedAt());
    }
}

