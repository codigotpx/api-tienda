package com.tienda.universitaria.api.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductDtos {

    public record ProductCreateRequest(
            @NotBlank String sku,
            @NotBlank String name,
            String description,
            @NotNull @DecimalMin(value = "0.01", message = "Price must be greater than zero") BigDecimal price,
            @NotNull UUID categoryId,
            Boolean active
    ) implements Serializable {}

    public record ProductUpdateRequest(
            String sku,
            String name,
            String description,
            @DecimalMin(value = "0.01", message = "Price must be greater than zero") BigDecimal price,
            UUID categoryId,
            Boolean active
    ) implements Serializable {}

    public record ProductResponse(
            UUID id,
            String sku,
            String name,
            String description,
            BigDecimal price,
            Boolean active,
            UUID categoryId,
            String categoryName
    ) implements Serializable {}
}
