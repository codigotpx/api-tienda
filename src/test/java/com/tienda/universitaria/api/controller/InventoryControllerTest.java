package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.InventoryController;
import com.tienda.universitaria.api.api.dto.InventoryDtos;
import com.tienda.universitaria.api.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
public class InventoryControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean InventoryService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();

        var req = new InventoryDtos.InventoryCreateRequest(10, 2);
        var res = new InventoryDtos.InventoryResponse(inventoryId, 10, 2, productId);

        when(service.create(eq(productId), any())).thenReturn(res);

        mvc.perform(post("/api/inventories/by-product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/inventories/" + inventoryId)))
                .andExpect(jsonPath("$.id").value(inventoryId.toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();

        var req = new InventoryDtos.InventoryUpdateRequest(15, 3);
        var res = new InventoryDtos.InventoryResponse(inventoryId, 15, 3, productId);

        when(service.update(eq(productId), any())).thenReturn(res);

        mvc.perform(put("/api/inventories/by-product/" + productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventoryId.toString()))
                .andExpect(jsonPath("$.availableStock").value(15));
    }

    @Test
    void getById_shouldReturn200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        var res = new InventoryDtos.InventoryResponse(inventoryId, 10, 2, UUID.randomUUID());

        when(service.get(inventoryId)).thenReturn(res);

        mvc.perform(get("/api/inventories/" + inventoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventoryId.toString()));
    }

    @Test
    void getByProductId_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();
        var res = new InventoryDtos.InventoryResponse(inventoryId, 10, 2, productId);

        when(service.getByProduct(productId)).thenReturn(res);

        mvc.perform(get("/api/inventories/by-product/" + productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventoryId.toString()));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new InventoryDtos.InventoryResponse(inventoryId, 10, 2, UUID.randomUUID())),
                PageRequest.of(0, 10),
                1
        );

        when(service.getAll(any())).thenReturn(page);

        mvc.perform(get("/api/inventories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(inventoryId.toString()));
    }

    @Test
    void getLowStock_shouldReturn200() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new InventoryDtos.InventoryResponse(inventoryId, 1, 5, UUID.randomUUID())),
                PageRequest.of(0, 10),
                1
        );

        when(service.getLowStock(any())).thenReturn(page);

        mvc.perform(get("/api/inventories/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(inventoryId.toString()));
    }

    @Test
    void adjustStock_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        UUID inventoryId = UUID.randomUUID();

        when(service.adjustStock(productId, 3))
                .thenReturn(new InventoryDtos.InventoryResponse(inventoryId, 13, 2, productId));

        mvc.perform(patch("/api/inventories/by-product/" + productId + "/adjust").param("delta", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(inventoryId.toString()))
                .andExpect(jsonPath("$.availableStock").value(13));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID inventoryId = UUID.randomUUID();
        doNothing().when(service).delete(inventoryId);

        mvc.perform(delete("/api/inventories/" + inventoryId))
                .andExpect(status().isNoContent());
    }
}

