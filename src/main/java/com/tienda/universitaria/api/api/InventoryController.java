package com.tienda.universitaria.api.api;

import  com.tienda.universitaria.api.api.dto.InventoryDtos.*;
import com.tienda.universitaria.api.service.InventoryService;
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
@RequestMapping("/api/inventories")
@RequiredArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/by-product/{productId}")
    public ResponseEntity<InventoryResponse> create(@RequestBody InventoryCreateRequest req,
                                                    UriComponentsBuilder builder,
                                                    @PathVariable UUID productId) {
        var inventoryCreated = service.create(productId, req);
        var location = builder.path("/api/inventories/{inventoryId}")
                .buildAndExpand(inventoryCreated.id())
                .toUri();

        return ResponseEntity.created(location).body(inventoryCreated);
    }

    @PutMapping("/by-product/{productId}")
    public ResponseEntity<InventoryResponse> update(@RequestBody InventoryUpdateRequest req,
                                                    @PathVariable UUID productId) {
        return ResponseEntity.ok(service.update(productId, req));
    }

    @GetMapping("/{inventoryId}")
    public ResponseEntity<InventoryResponse> getById(@PathVariable UUID inventoryId) {
        return ResponseEntity.ok(service.get(inventoryId));
    }

    @GetMapping("/by-product/{productId}")
    public ResponseEntity<InventoryResponse> getByProductId(@PathVariable UUID productId) {
        return ResponseEntity.ok(service.getByProduct(productId));
    }

    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> getAll(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getAll(pageable));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Page<InventoryResponse>> getLowStock(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(service.getLowStock(pageable));
    }

    @PatchMapping("/by-product/{productId}/adjust")
    public ResponseEntity<InventoryResponse> adjustStock(@PathVariable UUID productId, @RequestParam int delta) {
        return ResponseEntity.ok(service.adjustStock(productId, delta));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> delete(@PathVariable UUID inventoryId) {
        service.delete(inventoryId);
        return ResponseEntity.noContent().build();
    }
}
