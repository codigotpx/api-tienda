package com.tienda.universitaria.api.controller;

import com.tienda.universitaria.api.api.ReportController;
import com.tienda.universitaria.api.api.dto.ReportDtos;
import com.tienda.universitaria.api.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
public class ReportControllerTest {

    @Autowired MockMvc mvc;
    @MockitoBean ReportService service;

    @Test
    void getMonthlyIncome_shouldReturn200() throws Exception {
        when(service.getMonthlyIncome()).thenReturn(List.of(new ReportDtos.MonthlyIncomeResponse(2026, 1, new BigDecimal("100.00"))));

        mvc.perform(get("/api/reports/monthly-income"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].year").value(2026));
    }

    @Test
    void getTopCustomers_shouldReturn200() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(service.getTopCustomers()).thenReturn(List.of(new ReportDtos.TopCustomerResponse(customerId, "Camilo", "Cerpa", new BigDecimal("50.00"))));

        mvc.perform(get("/api/reports/top-customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()));
    }

    @Test
    void getTopCategoriesByVolume_shouldReturn200() throws Exception {
        when(service.getTopCategoriesByVolume()).thenReturn(List.of(new ReportDtos.TopCategoryVolumeResponse("Bebidas", 10)));

        mvc.perform(get("/api/reports/top-categories-by-volume"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryName").value("Bebidas"));
    }

    @Test
    void getBestSellingProducts_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        when(service.getBestSellingProducts(any(), any())).thenReturn(List.of(new ReportDtos.BestSellingProductResponse(productId, "SKU-1", "Agua", 10)));

        mvc.perform(get("/api/reports/best-selling-products")
                        .param("from", "2026-01-01T00:00:00")
                        .param("to", "2026-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId.toString()));
    }

    @Test
    void getLowStockProducts_shouldReturn200() throws Exception {
        UUID productId = UUID.randomUUID();
        when(service.getLowStockProducts()).thenReturn(List.of(new ReportDtos.LowStockProductResponse(productId, "Agua", "SKU-1", 1, 5)));

        mvc.perform(get("/api/reports/low-stock-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value(productId.toString()));
    }
}

