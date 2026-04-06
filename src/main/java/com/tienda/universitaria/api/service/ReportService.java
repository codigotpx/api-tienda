package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ReportDtos;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportService {

    List<ReportDtos.MonthlyIncomeResponse> getMonthlyIncome();

    List<ReportDtos.TopCustomerResponse> getTopCustomers();

    List<ReportDtos.TopCategoryVolumeResponse> getTopCategoriesByVolume();

    List<ReportDtos.BestSellingProductResponse> getBestSellingProducts(LocalDateTime from, LocalDateTime to);

    List<ReportDtos.LowStockProductResponse> getLowStockProducts();
}

