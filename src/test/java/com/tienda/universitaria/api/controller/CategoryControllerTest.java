package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.CategoryController;
import com.tienda.universitaria.api.api.dto.CategoryDtos;
import com.tienda.universitaria.api.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean CategoryService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CategoryDtos.CategoryCreateRequest("Bebidas", "Bebidas frias y calientes");
        var res = new CategoryDtos.CategoryResponse(id, "Bebidas", "Bebidas frias y calientes");

        when(service.create(any())).thenReturn(res);

        mvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/categories/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var req = new CategoryDtos.CategoryUpdateRequest("Snacks", "Dulces y salados");
        var res = new CategoryDtos.CategoryResponse(id, "Snacks", "Dulces y salados");

        when(service.update(eq(id), any())).thenReturn(res);

        mvc.perform(put("/api/categories/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Snacks"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new CategoryDtos.CategoryResponse(id, "Bebidas", "Bebidas frias y calientes");

        when(service.get(id)).thenReturn(res);

        mvc.perform(get("/api/categories/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getByName_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        var res = new CategoryDtos.CategoryResponse(id, "Bebidas", "Bebidas frias y calientes");

        when(service.getByName("Bebidas")).thenReturn(res);

        mvc.perform(get("/api/categories/search/by-name").param("name", "Bebidas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getAll_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getAll()).thenReturn(List.of(new CategoryDtos.CategoryResponse(id, "Bebidas", null)));

        mvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(service).delete(id);

        mvc.perform(delete("/api/categories/" + id))
                .andExpect(status().isNoContent());
    }
}

