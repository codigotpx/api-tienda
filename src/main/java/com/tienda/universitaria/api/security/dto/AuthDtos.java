package com.tienda.universitaria.api.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record RegisterClientRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RegisterAdminRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RegisterCoordinatorRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record AuthResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds
    ) {}
}
