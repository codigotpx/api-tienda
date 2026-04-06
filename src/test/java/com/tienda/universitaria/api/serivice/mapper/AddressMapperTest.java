package com.tienda.universitaria.api.serivice.mapper;

import com.tienda.universitaria.api.api.dto.AddressDtos;
import com.tienda.universitaria.api.domain.entities.Address;
import com.tienda.universitaria.api.domain.entities.Customer;
import com.tienda.universitaria.api.service.mapper.AddressMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {
    private final AddressMapper mapper = Mappers.getMapper(AddressMapper.class);

    @Test
    void toEntity_shouldMapCreate() {
        Address entity = mapper.toEntity(new AddressDtos.AddressCreateRequest(
                "Cra 1 # 2-3",
                "Bogota",
                "Cundinamarca",
                "110111",
                "Colombia"
        ));

        assertNull(entity.getId());
        assertNull(entity.getCustomer());
        assertEquals("Cra 1 # 2-3", entity.getStreet());
        assertEquals("Bogota", entity.getCity());
        assertEquals("Cundinamarca", entity.getState());
        assertEquals("110111", entity.getZip());
        assertEquals("Colombia", entity.getCountry());
    }

    @Test
    void toResponse_shouldMapEntity() {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID addressId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        Customer customer = Customer.builder().id(customerId).build();
        Address address = Address.builder()
                .id(addressId)
                .street("Cra 1 # 2-3")
                .city("Bogota")
                .state("Cundinamarca")
                .zip("110111")
                .country("Colombia")
                .customer(customer)
                .build();

        AddressDtos.AddressResponse dto = mapper.toResponse(address);

        assertEquals(addressId, dto.id());
        assertEquals("Cra 1 # 2-3", dto.street());
        assertEquals("Bogota", dto.city());
        assertEquals("Cundinamarca", dto.state());
        assertEquals("110111", dto.zip());
        assertEquals("Colombia", dto.country());
        assertEquals(customerId, dto.customerId());
    }

    @Test
    void patch_shouldIgnoreNulls() {
        UUID customerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Customer customer = Customer.builder().id(customerId).build();

        Address target = Address.builder()
                .street("Old Street")
                .city("Old City")
                .state("Old State")
                .zip("000000")
                .country("Old Country")
                .customer(customer)
                .build();

        mapper.patch(target, new AddressDtos.AddressUpdateRequest(
                null,
                "New City",
                null,
                "999999",
                null
        ));

        assertEquals("Old Street", target.getStreet());
        assertEquals("New City", target.getCity());
        assertEquals("Old State", target.getState());
        assertEquals("999999", target.getZip());
        assertEquals("Old Country", target.getCountry());
        assertSame(customer, target.getCustomer());
    }
}

