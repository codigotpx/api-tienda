package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CategoryDtos;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    CategoryDtos.CategoryResponse create(CategoryDtos.CategoryCreateRequest req);

    CategoryDtos.CategoryResponse update(UUID id, CategoryDtos.CategoryUpdateRequest req);

    CategoryDtos.CategoryResponse get(UUID id);

    CategoryDtos.CategoryResponse getByName(String name);

    List<CategoryDtos.CategoryResponse> getAll();

    void delete(UUID id);
}

