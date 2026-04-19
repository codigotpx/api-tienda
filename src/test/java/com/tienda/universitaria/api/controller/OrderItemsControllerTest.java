package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.OrderItemsController;
import com.tienda.universitaria.api.api.dto.OrderItemDtos;
import com.tienda.universitaria.api.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderItemsController.class)
public class OrderItemsControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean OrderItemService service;

    @Test
    void getByOrder_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();

        when(service.getByOrder(orderId)).thenReturn(List.of(
                new OrderItemDtos.OrderItemResponse(
                        orderItemId, UUID.randomUUID(), "Agua", 2,
                        new BigDecimal("2.50"), new BigDecimal("5.00")
                )
        ));

        mvc.perform(get("/api/orders/" + orderId + "/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(orderItemId.toString()));
    }

    @Test
    void addToOrder_shouldReturn201AndLocation() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        var req = new OrderItemDtos.OrderItemCreateRequest(productId, 2);
        var res = new OrderItemDtos.OrderItemResponse(
                orderItemId, productId, "Agua", 2,
                new BigDecimal("2.50"), new BigDecimal("5.00")
        );

        when(service.addToOrder(eq(orderId), any())).thenReturn(res);

        mvc.perform(post("/api/orders/" + orderId + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/orders/" + orderId + "/items/" + orderItemId)))
                .andExpect(jsonPath("$.id").value(orderItemId.toString()));
    }

    @Test
    void updateQuantity_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();

        var res = new OrderItemDtos.OrderItemResponse(
                orderItemId, UUID.randomUUID(), "Agua", 3,
                new BigDecimal("2.50"), new BigDecimal("7.50")
        );

        when(service.updateQuantity(orderId, orderItemId, 3)).thenReturn(res);

        mvc.perform(patch("/api/orders/" + orderId + "/items/" + orderItemId).param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderItemId.toString()))
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID orderItemId = UUID.randomUUID();

        doNothing().when(service).delete(orderId, orderItemId);

        mvc.perform(delete("/api/orders/" + orderId + "/items/" + orderItemId))
                .andExpect(status().isNoContent());
    }
}

