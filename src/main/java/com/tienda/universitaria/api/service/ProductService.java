package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ProductDtos;

import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductDtos.ProductResponse create(ProductDtos.ProductCreateRequest req);

    ProductDtos.ProductResponse update(UUID id, ProductDtos.ProductUpdateRequest req);

    ProductDtos.ProductResponse get(UUID id);

    ProductDtos.ProductResponse getBySku(String sku);

    List<ProductDtos.ProductResponse> getAll();

    List<ProductDtos.ProductResponse> getActive();

    List<ProductDtos.ProductResponse> getByCategory(UUID categoryId);

    List<ProductDtos.ProductResponse> getAllOrderByPriceAsc();

    List<ProductDtos.ProductResponse> getLowStock();

    ProductDtos.ProductResponse setActive(UUID id, boolean active);

    void delete(UUID id);
}

