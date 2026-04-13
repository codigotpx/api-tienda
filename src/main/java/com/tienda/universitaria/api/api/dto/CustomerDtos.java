package com.tienda.universitaria.api.api.dto;

import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.UUID;

public class CustomerDtos {
    public record CustomerCreateRequest(
            @NotBlank String firstName,
            @NotBlank String lastName,
            String phone,
            @NotBlank @Email String email
    ) implements Serializable {}

    public record CustomerUpdateRequest(
            String firstName,
            String lastName,
            String phone,
            @Email String email
    ) implements Serializable {}

    public record CustomerResponse(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phone,
            CustomerStatus status
    ) implements Serializable {}


}
