package com.tienda.universitaria.api.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

public class AddressDtos {

    public record AddressCreateRequest(
            @NotBlank String street,
            @NotBlank String city,
            String state,
            String zip,
            @NotBlank String country
    ) implements Serializable {}

    public record AddressUpdateRequest(
            String street,
            String city,
            String state,
            String zip,
            String country
    ) implements Serializable {}

    public record AddressResponse(
            UUID id,
            String street,
            String city,
            String state,
            String zip,
            String country,
            UUID customerId
    ) implements Serializable {}
}
