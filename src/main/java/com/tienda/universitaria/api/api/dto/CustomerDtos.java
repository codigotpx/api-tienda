package com.tienda.universitaria.api.api.dto;

import com.tienda.universitaria.api.domain.enums.CustomerStatus;

import java.util.UUID;

public class CustomerDtos {
    public record CustomerCreateRequest(
            String firstName,
            String lastName,
            String phone,
            String email
    ) {}

    public record CustomerUpdateRequest(
            String firstName,
            String lastName,
            String phone,
            String email
    ) {}

    public record CustomerResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            CustomerStatus status
    ) {}


}
