package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.CustomerDtos.*;
import com.tienda.universitaria.api.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@RestController
@RequestMapping("api/customers")
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

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

}
