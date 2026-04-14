package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.CategoryDtos;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.service.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryDtos.CategoryResponse create(CategoryDtos.CategoryCreateRequest req) {
        if (req == null) {
            throw new ValidationException("CategoryCreateRequest must not be null");
        }
        if (req.name() == null || req.name().isBlank()) {
            throw new ValidationException("name must not be blank");
        }
        if (categoryRepository.existsByName(req.name())) {
            throw new ConflictException("Category name already exists: " + req.name());
        }

        Category category = categoryMapper.toEntity(req);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryDtos.CategoryResponse update(UUID id, CategoryDtos.CategoryUpdateRequest req) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }
        if (req == null) {
            throw new ValidationException("CategoryUpdateRequest must not be null");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));

        if (req.name() != null && !req.name().isBlank()) {
            String newName = req.name();
            String currentName = category.getName();
            if (!newName.equalsIgnoreCase(currentName) && categoryRepository.existsByName(newName)) {
                throw new ConflictException("Category name already exists: " + newName);
            }
        }

        categoryMapper.patch(category, req);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDtos.CategoryResponse get(UUID id) {
        if (id == null) {
            throw new ValidationException("id must not be null");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        return categoryMapper.toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDtos.CategoryResponse getByName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("name must not be blank");
        }

        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found for name: " + name));
        return categoryMapper.toResponse(category);
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
        if (id == null) {
            throw new ValidationException("id must not be null");
        }

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.delete(category);
    }
}

