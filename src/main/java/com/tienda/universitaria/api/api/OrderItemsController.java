package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.OrderItemDtos.OrderItemCreateRequest;
import com.tienda.universitaria.api.api.dto.OrderItemDtos.OrderItemResponse;
import com.tienda.universitaria.api.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class OrderItemsController {

    private final OrderItemService service;

    @GetMapping({"/orderItems/{orderId}", "/orders/{orderId}/items"})
    public ResponseEntity<List<OrderItemResponse>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(service.getByOrder(orderId));
    }

    @PostMapping({"/orderItems/{orderId}", "/orders/{orderId}/items"})
    public ResponseEntity<OrderItemResponse> addToOrder(@PathVariable UUID orderId,
                                                        @RequestBody OrderItemCreateRequest req,
                                                        UriComponentsBuilder builder) {
        var created = service.addToOrder(orderId, req);
        var location = builder.path("/api/orders/{orderId}/items/{id}")
                .buildAndExpand(orderId, created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping({"/orderItems/{orderId}/{orderItemId}", "/orders/{orderId}/items/{orderItemId}"})
    public ResponseEntity<OrderItemResponse> updateQuantity(@PathVariable UUID orderId,
                                                           @PathVariable UUID orderItemId,
                                                           @RequestParam int quantity) {
        return ResponseEntity.ok(service.updateQuantity(orderId, orderItemId, quantity));
    }

    @DeleteMapping({"/orderItems/{orderId}/{orderItemId}", "/orders/{orderId}/items/{orderItemId}"})
    public ResponseEntity<Void> delete(@PathVariable UUID orderId,
                                       @PathVariable UUID orderItemId) {
        service.delete(orderId, orderItemId);
        return ResponseEntity.noContent().build();
    }
}

