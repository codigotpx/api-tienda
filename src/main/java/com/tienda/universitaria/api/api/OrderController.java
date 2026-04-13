package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.OrderDtos.*;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderController {
    private final OrderService service;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@RequestBody OrderCreateRequest req,
                                                UriComponentsBuilder builder) {
        var created = service.create(req);
        var location = builder.path("/api/orders/{id}").buildAndExpand(created.id()).toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable UUID customerId) {
        return ResponseEntity.ok(service.getByCustomer(customerId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> findByFilters(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal
    ) {
        return ResponseEntity.ok(service.findByFilters(customerId, status, from, to, minTotal, maxTotal));
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> setStatus(@PathVariable UUID orderId,
                                                   @RequestParam OrderStatus status,
                                                   @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(service.setStatus(orderId, status, notes));
    }
}
