package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.ProductController;
import com.tienda.universitaria.api.api.dto.ProductDtos;
import com.tienda.universitaria.api.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean ProductService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        var req = new ProductDtos.ProductCreateRequest(
                "SKU-1",
                "Agua",
                "Agua 500ml",
                new BigDecimal("2.50"),
                categoryId,
                true
        );
        var res = new ProductDtos.ProductResponse(id, "SKU-1", "Agua", "Agua 500ml",
                new BigDecimal("2.50"), true, categoryId, "Bebidas");

        when(service.create(any())).thenReturn(res);

        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/products/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        var req = new ProductDtos.ProductUpdateRequest("SKU-2", "Galletas", null, new BigDecimal("3.00"), categoryId, true);
        var res = new ProductDtos.ProductResponse(id, "SKU-2", "Galletas", null, new BigDecimal("3.00"), true, categoryId, "Snacks");

        when(service.update(eq(id), any())).thenReturn(res);

        mvc.perform(put("/api/products/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.sku").value("SKU-2"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, UUID.randomUUID(), "Bebidas");

        when(service.get(id)).thenReturn(res);

        mvc.perform(get("/api/products/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getBySku_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, UUID.randomUUID(), "Bebidas");

        when(service.getBySku("SKU-1")).thenReturn(res);

        mvc.perform(get("/api/products/search/by-sku").param("sku", "SKU-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, UUID.randomUUID(), "Bebidas")),
                PageRequest.of(0, 10),
                1
        );

        when(service.getAll(any())).thenReturn(page);

        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void getActive_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, UUID.randomUUID(), "Bebidas")),
                PageRequest.of(0, 10),
                1
        );

        when(service.getActive(any())).thenReturn(page);

        mvc.perform(get("/api/products/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void getByCategory_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, categoryId, "Bebidas")),
                PageRequest.of(0, 10),
                1
        );

        when(service.getByCategory(eq(categoryId), any())).thenReturn(page);

        mvc.perform(get("/api/products/by-category/" + categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void getLowStock_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var page = new PageImpl<>(
                List.of(new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), true, UUID.randomUUID(), "Bebidas")),
                PageRequest.of(0, 10),
                1
        );

        when(service.getLowStock(any())).thenReturn(page);

        mvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(id.toString()));
    }

    @Test
    void setActive_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new ProductDtos.ProductResponse(id, "SKU-1", "Agua", null, new BigDecimal("2.50"), false, UUID.randomUUID(), "Bebidas");

        when(service.setActive(id, false)).thenReturn(res);

        mvc.perform(patch("/api/products/" + id + "/active").param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mvc.perform(delete("/api/products/" + id))
                .andExpect(status().isNoContent());
    }
}

