package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.AddressController;
import com.tienda.universitaria.api.api.dto.AddressDtos;
import com.tienda.universitaria.api.service.AddressService;
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

@WebMvcTest(AddressController.class)
public class AddressControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockitoBean AddressService service;

    @Test
    void create_shouldReturn201AndLocation() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId  = UUID.randomUUID();
        var req = new AddressDtos.AddressCreateRequest
                ("Calle 10", "Valledupar", "Cesar", "110111", "Colombia");
        var res = new AddressDtos
                .AddressResponse(addressId, "Calle 10", "Valledupar", "Cesar"
                , "110111", "Colombia", customerId);

        when(service.create(eq(customerId), any())).thenReturn(res);

        mvc.perform(post("/api/addresses/" + customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/addresses/" + addressId)))
                .andExpect(jsonPath("$.id").value(addressId.toString()));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId  = UUID.randomUUID();
        var req = new AddressDtos.AddressCreateRequest
                ("Calle 10", "Valledupar", "Cesar", "110111", "Colombia");
        var res = new AddressDtos
                .AddressResponse(addressId, "Calle 10", "Valledupar", "Cesar"
                , "110111", "Colombia", customerId);
        when(service.update(eq(customerId), eq(addressId), any())).thenReturn(res);

        mvc.perform(put("/api/addresses/" + customerId + "/" + addressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.city").value("Valledupar"));
    }

    @Test
    void get_shouldReturn200() throws Exception {
        UUID addressId = UUID.randomUUID();
        var res = new AddressDtos
                .AddressResponse(addressId, "Calle 10", "Valledupar", "Cesar"
                , "110111", "Colombia", UUID.randomUUID());

        when(service.get(addressId)).thenReturn(res);

        mvc.perform(get("/api/addresses/" + addressId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(addressId.toString()));
    }

    @Test
    void getByCustomer_shouldReturn200() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId  = UUID.randomUUID();
        var res = new AddressDtos
                .AddressResponse(addressId, "Calle 10", "Valledupar", "Cesar"
                , "110111", "Colombia", customerId);
        when(service.getByCustomer(customerId)).thenReturn(List.of(res));

        mvc.perform(get("/api/addresses/customer/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(addressId.toString()));
    }

    @Test
    void delete_shouldReturn204() throws Exception {
        UUID customerId = UUID.randomUUID();
        UUID addressId  = UUID.randomUUID();

        doNothing().when(service).delete(customerId, addressId);

        mvc.perform(delete("/api/addresses/" + customerId + "/" + addressId))
                .andExpect(status().isNoContent());
    }
}