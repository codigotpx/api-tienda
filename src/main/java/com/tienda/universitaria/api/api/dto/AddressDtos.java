package com.tienda.universitaria.api.api.dto;

import java.util.UUID;

public class AddressDtos {
    public record AddressCreateRequest(
          String street,
          String city,
          String state,
          String zip,
          String country
    ) {}

    public record AddressUpdateRequest(
          String street,
          String city,
          String state,
          String zip,
          String country
    ) {}

    public record AddressResponse(
          UUID id,
          String street,
          String city,
          String state,
          String zip,
          String country,
          UUID customerId
    ){}
}
