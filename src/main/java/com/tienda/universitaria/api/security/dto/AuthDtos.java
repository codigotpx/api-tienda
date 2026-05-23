package com.tienda.universitaria.api.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {

    public record RegisterClientRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {}

    public record RegisterClientWithProfileRequest(
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotBlank String firstName,
            @NotBlank String lastName,
            @NotBlank String phone
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

    public record MeResponse(
            String username,
            java.util.Set<String> roles
    ) {}
}
