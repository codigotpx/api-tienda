package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.CustomerController;
import com.tienda.universitaria.api.api.dto.CustomerDtos;
import com.tienda.universitaria.api.domain.enums.CustomerStatus;
import com.tienda.universitaria.api.service.CustomerService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @MockitoBean
    CustomerService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        var req = new CustomerDtos.CustomerCreateRequest("Camilo", "Andres",
                "12345678", "cerpacamilo3@gmail.com");
        UUID id = UUID.randomUUID();
        var res = new CustomerDtos.CustomerResponse(id, "Camilo", "Andres",
                "cerpacamilo3@gmail.com", "123456789", CustomerStatus.ACTIVE);

        when(service.create(any())).thenReturn(res);

        mvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/customers/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        var req = new CustomerDtos.CustomerUpdateRequest("Camilo", "Andres",
                "cerpacamilo3@gmail.com", "123456789");

        var res = new CustomerDtos.CustomerResponse(id, "Camilo", "Andres",
                "cerpacamilo3@gmail.com", "123456789", CustomerStatus.ACTIVE);

        when(service.update(eq(id), any())).thenReturn(res);

        mvc.perform(put("/api/customers/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.firstName").value("Camilo"))
                .andExpect(jsonPath("$.email").value("cerpacamilo3@gmail.com"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        UUID id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

        when(service.get(id)).thenReturn(new CustomerDtos.CustomerResponse(id, "Camilo",
                "Cerpa", "cerpacamilo3@gmail.com", "123456789", CustomerStatus.ACTIVE));

        mvc.perform(get("/api/customers/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }


}
