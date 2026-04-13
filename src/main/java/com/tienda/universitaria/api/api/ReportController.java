package com.tienda.universitaria.api.api;

import com.tienda.universitaria.api.api.dto.ReportDtos.*;
import com.tienda.universitaria.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Validated
public class ReportController {

    private final ReportService service;

    @GetMapping("/monthly-income")
    public ResponseEntity<List<MonthlyIncomeResponse>> getMonthlyIncome() {
        return ResponseEntity.ok(service.getMonthlyIncome());
    }

    @GetMapping("/top-customers")
    public ResponseEntity<List<TopCustomerResponse>> getTopCustomers() {
        return ResponseEntity.ok(service.getTopCustomers());
    }

    @GetMapping("/top-categories-by-volume")
    public ResponseEntity<List<TopCategoryVolumeResponse>> getTopCategoriesByVolume() {
        return ResponseEntity.ok(service.getTopCategoriesByVolume());
    }

    @GetMapping("/best-selling-products")
    public ResponseEntity<List<BestSellingProductResponse>> getBestSellingProducts(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(service.getBestSellingProducts(from, to));
    }

    @GetMapping("/low-stock-products")
    public ResponseEntity<List<LowStockProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(service.getLowStockProducts());
    }
}

