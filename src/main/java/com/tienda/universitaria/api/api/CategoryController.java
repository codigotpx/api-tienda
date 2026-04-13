package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.CategoryDtos.*;
import com.tienda.universitaria.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/controllers")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService service;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CategoryCreateRequest req,
                                                   UriComponentsBuilder builder) {
        var categoryCreated = service.create(req);
        var location = builder.path("/api/controllers/{id}").buildAndExpand(categoryCreated.id()).toUri();

        return ResponseEntity.created(location).body(categoryCreated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable UUID id,
                                                   @RequestBody CategoryUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse>  get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CategoryResponse> getByName(@PathVariable String name) {
        return ResponseEntity.ok(service.getByName(name));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        return ResponseEntity.noContent().build();
    }
}
