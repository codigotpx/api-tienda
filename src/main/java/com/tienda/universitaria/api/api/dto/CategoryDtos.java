package com.tienda.universitaria.api.api.dto;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.UUID;

public class CategoryDtos {

    public record CategoryCreateRequest(
            @NotBlank String name,
            String description
    ) implements Serializable {}

    public record CategoryUpdateRequest(
            String name,
            String description
    ) implements Serializable {}

    public record CategoryResponse(
            UUID id,
            String name,
            String description
    ) implements Serializable {}
}