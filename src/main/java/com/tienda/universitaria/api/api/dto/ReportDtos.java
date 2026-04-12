package com.tienda.universitaria.api.api.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

public class ReportDtos {

    /**
     * Derived from {@code OrderItemsRepository.findBestSellingProducts(...)}.
     * totalSold comes from SUM(quantity) and is typically a Long in JPA projections.
     */
    public record BestSellingProductResponse(
            UUID productId,
            String sku,
            String name,
            long totalSold
    ) implements Serializable {}

    /**
     * Derived from {@code OrderRepository.findMonthlyIncomeRaw()}.
     */
    public record MonthlyIncomeResponse(
            int year,
            int month,
            BigDecimal total
    ) implements Serializable {}

    /**
     * Derived from {@code OrderRepository.findTopCustomersRaw()}.
     */
    public record TopCustomerResponse(
            UUID customerId,
            String firstName,
            String lastName,
            BigDecimal total
    ) implements Serializable {}

    /**
     * Derived from {@code OrderRepository.findTopCategoriesByVolumeRaw()}.
     */
    public record TopCategoryVolumeResponse(
            String categoryName,
            long totalQuantity
    ) implements Serializable {}

    /**
     * Derived from {@code InventoryRepository.findLowStockProductsRaw()}.
     */
    public record LowStockProductResponse(
            UUID productId,
            String name,
            String sku,
            int availableStock,
            int minimumStock
    ) implements Serializable {}
}
