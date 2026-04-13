package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos.OrderStatusHistoryResponse;
import com.tienda.universitaria.api.service.OrderStatusHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Validated
public class OrderStatusHistoryController {

    private final OrderStatusHistoryService service;

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderStatusHistoryResponse>> getByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(service.getByOrder(orderId));
    }
}

