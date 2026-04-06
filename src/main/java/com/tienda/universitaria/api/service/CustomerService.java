package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;

import java.util.List;
import java.util.UUID;

public interface CustomerService {
    CustomerDtos.CustomerResponse create(CustomerDtos.CustomerCreateRequest req);

    CustomerDtos.CustomerResponse update(UUID id, CustomerDtos.CustomerUpdateRequest req);

    CustomerDtos.CustomerResponse get(UUID id);

    CustomerDtos.CustomerResponse getByEmail(String email);

    List<CustomerDtos.CustomerResponse> getAll();

    List<CustomerDtos.CustomerResponse> getByStatus(CustomerStatus status);

    CustomerDtos.CustomerResponse setStatus(UUID id, CustomerStatus status);

    void delete(UUID id);
}
