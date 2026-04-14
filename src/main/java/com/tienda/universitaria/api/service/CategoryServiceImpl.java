package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CategoryDtos;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDtos.CategoryResponse create(CategoryDtos.CategoryCreateRequest req) {
        if (categoryRepository.existsByName(req.name()))
            throw new ConflictException("Category name already exists: " + req.name());

        return categoryMapper.toResponse(categoryRepository.save(categoryMapper.toEntity(req)));
    }

    @Override
    public CategoryDtos.CategoryResponse update(UUID id, CategoryDtos.CategoryUpdateRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (req.name() != null && !req.name().isBlank()) {
            if (!req.name().equalsIgnoreCase(category.getName()) && categoryRepository.existsByName(req.name()))
                throw new ConflictException("Category name already exists: " + req.name());
        }

        categoryMapper.patch(category, req);
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDtos.CategoryResponse get(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDtos.CategoryResponse getByName(String name) {
        return categoryRepository.findByName(name)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for name: " + name));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDtos.CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.delete(category);
    }
}
