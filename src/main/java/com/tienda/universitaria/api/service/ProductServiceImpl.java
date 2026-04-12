package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ProductDtos;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderItemsRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.ProductMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;
    private final OrderItemsRepository orderItemsRepository;

    @Override
    public ProductDtos.ProductResponse create(ProductDtos.ProductCreateRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("ProductCreateRequest must not be null");
        }
        if (req.sku() == null || req.sku().isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }
        if (req.categoryId() == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        if (productRepository.findBySku(req.sku()).isPresent()) {
            throw new IllegalArgumentException("Product sku already exists: " + req.sku());
        }

        if (req.price() == null || req.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + req.categoryId()));

        Product product = productMapper.toEntity(req);
        product.setCategory(category);
        if (req.active() != null) {
            product.setActive(req.active());
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductDtos.ProductResponse update(UUID id, ProductDtos.ProductUpdateRequest req) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        if (req == null) {
            throw new IllegalArgumentException("ProductUpdateRequest must not be null");
        }

        if (req.price() != null && req.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        if (req.categoryId() != null) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Category not found: " + req.categoryId()));
            product.setCategory(category);
        }

        productMapper.patch(product, req);
        if (req.active() != null) {
            product.setActive(req.active());
        }

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDtos.ProductResponse get(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDtos.ProductResponse getBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("sku must not be blank");
        }

        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found for sku: " + sku));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDtos.ProductResponse> getAll(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDtos.ProductResponse> getActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDtos.ProductResponse> getByCategory(UUID categoryId, Pageable pageable) {
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId must not be null");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found: " + categoryId);
        }

        return productRepository.findByCategoryId(categoryId, pageable).map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductDtos.ProductResponse> getAllOrderByPriceAsc() {
        return productRepository.findAllByOrderByPriceAsc().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDtos.ProductResponse> getLowStock(Pageable pageable) {
        return productRepository.findProductsWithLowStock(pageable).map(productMapper::toResponse);
    }

    @Override
    public ProductDtos.ProductResponse setActive(UUID id, boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        if (!active) {
            boolean hasActiveOrders = orderItemsRepository.existsByProductIdAndOrderStatusIn(
                    id,
                    List.of(OrderStatus.CREATED, OrderStatus.PAID, OrderStatus.SHIPPED)
            );
            if (hasActiveOrders) {
                throw new IllegalArgumentException(
                        "Cannot deactivate product with active orders: " + id);
            }
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        product.setActive(active);

        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("id must not be null");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        productRepository.delete(product);
    }
}

