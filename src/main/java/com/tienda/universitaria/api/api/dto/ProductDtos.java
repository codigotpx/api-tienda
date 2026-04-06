package com.tienda.universitaria.api.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductDtos {
    public record ProductCreateRequest(
            String sku,
            String name,
            String description,
            BigDecimal price,
            UUID categoryId,
            Boolean active
    ) {}
    public record ProductUpdateRequest(
            String sku,
            String name,
            String description,
            BigDecimal price,
            UUID categoryId,
            Boolean active
    ) {}
    public record ProductResponse(
            UUID id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            Boolean active,
            UUID categoryId,
            String categoryName
    ) {}
}
