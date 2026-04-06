package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.service.mapper.CustomerMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerMapperTest {
    private CustomerMapper mapper = Mappers.getMapper(CustomerMapper.class);

    @Test
    void toEntity_shouldMapCreate() {
        Customer c = mapper.toEntity(new CustomerDtos.CustomerCreateRequest("Camilo",
                "Cerpa",
                "1234567", "prueba@gmail.com"));
        assertNull(c.getId());
        assertEquals("Camilo", c.getFirstName());
        assertEquals("Cerpa", c.getLastName());
        assertEquals("1234567", c.getPhone());
        assertEquals("prueba@gmail.com", c.getEmail());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        var c = Customer.builder()
                .id(id)
                .firstName("Camilo")
                .lastName("Cerpa")
                .phone("1234567")
                .email("prueba@gmail.com")
                .status(CustomerStatus.ACTIVE)
                .build();
        CustomerDtos.CustomerResponse dto = mapper.toResponse(c);
        assertEquals(id, dto.id());
        assertEquals("Camilo", dto.firstName());
        assertEquals("Cerpa", dto.lastName());
        assertEquals("prueba@gmail.com", dto.email());
        assertEquals("1234567", dto.phone());
        assertEquals(CustomerStatus.ACTIVE, dto.status());
    }
}
