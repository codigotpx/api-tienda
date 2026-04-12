package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.ProductDtos.*;
import com.tienda.universitaria.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
public class ProductController {
    private final ProductService service;

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest req,
                                                  UriComponentsBuilder builder) {
        var productCreated = service.create(req);
        var location = builder.path("/api/products/{id}").buildAndExpand(productCreated.id()).toUri();

        return ResponseEntity.created(location).body(productCreated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(@PathVariable UUID id,
                                                  @RequestBody ProductUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/search/by-sku")
    public ResponseEntity<ProductResponse> getBySku(@RequestParam String sku) {
        return ResponseEntity.ok(service.getBySku(sku));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<ProductResponse>> getActive(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getActive(pageable));
    }

    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getByCategory(@PathVariable UUID categoryId,
                                                               @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getByCategory(categoryId, pageable));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<ProductResponse>> getLowStock(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getLowStock(pageable));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ProductResponse> setActive(@PathVariable UUID id, @RequestParam boolean active) {
        return ResponseEntity.ok(service.setActive(id, active));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
