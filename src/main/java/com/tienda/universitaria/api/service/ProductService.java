package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ProductDtos;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductDtos.ProductResponse create(ProductDtos.ProductCreateRequest req);

    ProductDtos.ProductResponse update(UUID id, ProductDtos.ProductUpdateRequest req);

    ProductDtos.ProductResponse get(UUID id);

    ProductDtos.ProductResponse getBySku(String sku);

    Page<ProductDtos.ProductResponse> getAll(Pageable pageable);

    Page<ProductDtos.ProductResponse> getActive(Pageable pageable);

    Page<ProductDtos.ProductResponse> getByCategory(UUID categoryId, Pageable pageable);

    List<ProductDtos.ProductResponse> getAllOrderByPriceAsc();

    Page<ProductDtos.ProductResponse> getLowStock(Pageable pageable);

    ProductDtos.ProductResponse setActive(UUID id, boolean active);

    void delete(UUID id);
}

