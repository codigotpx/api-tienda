package com.tienda.universitaria.api.api.dto;

import java.io.Serializable;
import java.util.UUID;

public class AddressDtos {
    public record AddressCreateRequest(
          String street,
          String city,
          String state,
          String zip,
          String country
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
    ) implements Serializable{}
}
