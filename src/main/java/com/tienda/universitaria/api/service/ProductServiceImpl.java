package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ProductDtos;
import com.tienda.universitaria.api.api.exception.BusinessException;
import com.tienda.universitaria.api.api.exception.ConflictException;
import com.tienda.universitaria.api.api.exception.ResourceNotFoundException;
import com.tienda.universitaria.api.domain.entities.Category;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.domain.repositories.CategoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderItemsRepository;
import com.tienda.universitaria.api.domain.repositories.ProductRepository;
import com.tienda.universitaria.api.service.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        if (productRepository.findBySku(req.sku()).isPresent())
            throw new ConflictException("Product sku already exists: " + req.sku());

        Category category = categoryRepository.findById(req.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.categoryId()));

        Product product = productMapper.toEntity(req);
        product.setCategory(category);
        if (req.active() != null) product.setActive(req.active());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductDtos.ProductResponse update(UUID id, ProductDtos.ProductUpdateRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));

        if (req.categoryId() != null) {
            Category category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.categoryId()));
            product.setCategory(category);
        }

        productMapper.patch(product, req);
        if (req.active() != null) product.setActive(req.active());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDtos.ProductResponse get(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDtos.ProductResponse getBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found for sku: " + sku));
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
        if (!categoryRepository.existsById(categoryId))
            throw new ResourceNotFoundException("Category not found: " + categoryId);

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
        if (!active) {
            boolean hasActiveOrders = orderItemsRepository.existsByProductIdAndOrderStatusIn(
                    id, List.of(OrderStatus.CREATED, OrderStatus.PAID, OrderStatus.SHIPPED));
            if (hasActiveOrders)
                throw new BusinessException("Cannot deactivate product with active orders: " + id);
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        product.setActive(active);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepository.delete(product);
    }
}
