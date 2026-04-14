package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.tienda.universitaria.api.api.dto.AddressDtos.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
@Validated
public class AddressController {
    private final AddressService service;

    @PostMapping("/{customerId}")
    public ResponseEntity<AddressResponse> create(@RequestBody AddressCreateRequest req,
                                                  UriComponentsBuilder builder, @PathVariable UUID customerId) {
        var addressCreated = service.create(customerId, req);
        var location = builder.path("/api/addresses/{id}").buildAndExpand(addressCreated.id()).toUri();

        return ResponseEntity.created(location).body(addressCreated);
    }

    @PutMapping("/{customerId}/{addressId}")
    public ResponseEntity<AddressResponse> update(@PathVariable UUID customerId, @PathVariable UUID addressId,
                                                  @RequestBody AddressUpdateRequest req) {
        return ResponseEntity.ok(service.update(customerId, addressId, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AddressResponse>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(service.getByCustomer(customerId));
    }

    @DeleteMapping("/{customerId}/{addressId}")
    public ResponseEntity<Void> delete(@PathVariable UUID customerId,
                                       @PathVariable UUID addressId) {
        service.delete(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
