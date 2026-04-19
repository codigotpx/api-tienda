package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.OrderStatusHistoryController;
import com.tienda.universitaria.api.api.dto.OrderStatusHistoryDtos;
import com.tienda.universitaria.api.domain.enums.OrderStatus;
import com.tienda.universitaria.api.service.OrderStatusHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderStatusHistoryController.class)
public class OrderStatusHistoryControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean OrderStatusHistoryService service;

    @Test
    void getByOrder_shouldReturn200() throws Exception {
        UUID orderId = UUID.randomUUID();
        UUID historyId = UUID.randomUUID();

        when(service.getByOrder(orderId)).thenReturn(List.of(
                new OrderStatusHistoryDtos.OrderStatusHistoryResponse(
                        historyId, OrderStatus.CREATED, OrderStatus.PAID, "ok", LocalDateTime.now()
                )
        ));

        mvc.perform(get("/api/orders/" + orderId + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(historyId.toString()));
    }
}
