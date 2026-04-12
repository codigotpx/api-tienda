package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.CustomerDtos.*;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Validated
public class CustomerController {
    private final CustomerService service;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@RequestBody CustomerCreateRequest req,
                                                   UriComponentsBuilder builder) {
        var customerCreated = service.create(req);
        var location = builder.path("/api/customers/{id}").buildAndExpand(customerCreated.id()).toUri();

        return ResponseEntity.created(location).body(customerCreated);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable UUID id, @RequestBody CustomerUpdateRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/search/by-email")
    public ResponseEntity<CustomerResponse> getByEmail(@RequestParam String email) {
        return ResponseEntity.ok(service.getByEmail(email));
    }

    @GetMapping("/search/by-status")
    public ResponseEntity<List<CustomerResponse>> getByStatus(@RequestParam CustomerStatus status) {
        return ResponseEntity.ok(service.getByStatus(status));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> setStatus(@PathVariable UUID id, @RequestParam CustomerStatus status) {
        return ResponseEntity.ok(service.setStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
