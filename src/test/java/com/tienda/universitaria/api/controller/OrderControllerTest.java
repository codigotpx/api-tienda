package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.OrderController;
import com.tienda.universitaria.api.api.dto.OrderDtos;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean OrderService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();

        var req = new OrderDtos.OrderCreateRequest(
                customerId,
                addressId,
                List.of(new OrderItemDtos.OrderItemCreateRequest(productId, 2))
        );

        var res = new OrderDtos.OrderResponse(
                orderId,
                new BigDecimal("5.00"),
                OrderStatus.CREATED,
                customerId,
                "Camilo Cerpa",
                addressId,
                List.of(new OrderItemDtos.OrderItemResponse(
                        orderItemId,
                        productId,
                        "Agua",
                        2,
                        new BigDecimal("2.50"),
                        new BigDecimal("5.00")
                )),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(service.create(any())).thenReturn(res);

        mvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/orders/" + orderId)))
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        var res = new OrderDtos.OrderResponse(
                orderId,
                new BigDecimal("1.00"),
                OrderStatus.CREATED,
                UUID.randomUUID(),
                "Customer",
                UUID.randomUUID(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(service.get(orderId)).thenReturn(res);

        mvc.perform(get("/api/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(service.getAll()).thenReturn(List.of(new OrderDtos.OrderResponse(
                orderId, new BigDecimal("1.00"), OrderStatus.CREATED, UUID.randomUUID(),
                "Customer", UUID.randomUUID(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        )));

        mvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()));
    }

    @Test
    void getByCustomer_shouldReturn200() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        when(service.getByCustomer(customerId)).thenReturn(List.of(new OrderDtos.OrderResponse(
                orderId, new BigDecimal("1.00"), OrderStatus.CREATED, customerId,
                "Customer", UUID.randomUUID(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        )));

        mvc.perform(get("/api/orders/by-customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()));
    }

    @Test
    void findByFilters_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        when(service.findByFilters(
                any(), any(), any(), any(), any(), any()
        )).thenReturn(List.of(new OrderDtos.OrderResponse(
                orderId, new BigDecimal("1.00"), OrderStatus.CREATED, UUID.randomUUID(),
                "Customer", UUID.randomUUID(), List.of(), LocalDateTime.now(), LocalDateTime.now()
        )));

        mvc.perform(get("/api/orders/search")
                        .param("status", "CREATED")
                        .param("minTotal", "1.00")
                        .param("maxTotal", "10.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderId.toString()));
    }

    @Test
    void setStatus_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        var res = new OrderDtos.OrderResponse(
                orderId,
                new BigDecimal("1.00"),
                OrderStatus.PAID,
                UUID.randomUUID(),
                "Customer",
                UUID.randomUUID(),
                List.of(),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(service.setStatus(orderId, OrderStatus.PAID, "ok")).thenReturn(res);

        mvc.perform(patch("/api/orders/" + orderId + "/status")
                        .param("status", "PAID")
                        .param("notes", "ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()))
                .andExpect(jsonPath("$.status").value("PAID"));
    }
}
