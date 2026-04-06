package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.AddressDtos;

import java.util.List;
import java.util.UUID;

public interface AddressService {

    AddressDtos.AddressResponse create(UUID customerId, AddressDtos.AddressCreateRequest req);

    AddressDtos.AddressResponse update(UUID customerId, UUID addressId, AddressDtos.AddressUpdateRequest req);

    AddressDtos.AddressResponse get(UUID addressId);

    List<AddressDtos.AddressResponse> getByCustomer(UUID customerId);

    void delete(UUID customerId, UUID addressId);
}

