package com.tienda.universitaria.api.api.dto;

import java.util.UUID;

public class CategoryDtos {
    public record CategoryCreateRequest(
            String name,
            String description
    ) {}
    public record CategoryUpdateRequest(
            String name,
            String description
    ) {}
    public record CategoryResponse(
            UUID id,
            String name,
            String description
    ) {}
}
