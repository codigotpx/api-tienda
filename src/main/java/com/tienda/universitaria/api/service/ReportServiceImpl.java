package com.tienda.universitaria.api.service;

import com.tienda.universitaria.api.api.dto.ReportDtos;
import com.tienda.universitaria.api.api.exception.ValidationException;
import com.tienda.universitaria.api.domain.entities.Product;
import com.tienda.universitaria.api.domain.repositories.InventoryRepository;
import com.tienda.universitaria.api.domain.repositories.OrderItemsRepository;
import com.tienda.universitaria.api.domain.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public List<ReportDtos.MonthlyIncomeResponse> getMonthlyIncome() {
        return orderRepository.findMonthlyIncomeRaw().stream()
                .map(this::toMonthlyIncome)
                .toList();
    }

    @Override
    public List<ReportDtos.TopCustomerResponse> getTopCustomers() {
        return orderRepository.findTopCustomersRaw().stream()
                .map(this::toTopCustomer)
                .toList();
    }

    @Override
    public List<ReportDtos.TopCategoryVolumeResponse> getTopCategoriesByVolume() {
        return orderRepository.findTopCategoriesByVolumeRaw().stream()
                .map(this::toTopCategoryVolume)
                .toList();
    }

    @Override
    public List<ReportDtos.BestSellingProductResponse> getBestSellingProducts(LocalDateTime from, LocalDateTime to) {
        if (from == null) throw new ValidationException("from must not be null");
        if (to == null)   throw new ValidationException("to must not be null");
        if (from.isAfter(to)) throw new ValidationException("from must be before or equal to to");

        return orderItemsRepository.findBestSellingProducts(from, to).stream()
                .map(this::toBestSellingProduct)
                .toList();
    }

    @Override
    public List<ReportDtos.LowStockProductResponse> getLowStockProducts() {
        return inventoryRepository.findLowStockProductsRaw().stream()
                .map(this::toLowStockProduct)
                .toList();
    }

    private ReportDtos.MonthlyIncomeResponse toMonthlyIncome(Object[] row) {
        // [year, month, sum(total)]
        int year = toInt(row, 0);
        int month = toInt(row, 1);
        BigDecimal total = toBigDecimal(row, 2);
        return new ReportDtos.MonthlyIncomeResponse(year, month, total);
    }

    private ReportDtos.TopCustomerResponse toTopCustomer(Object[] row) {
        // [customerId, firstName, lastName, sum(total)]
        UUID customerId = (UUID) row[0];
        String firstName = (String) row[1];
        String lastName = (String) row[2];
        BigDecimal total = toBigDecimal(row, 3);
        return new ReportDtos.TopCustomerResponse(customerId, firstName, lastName, total);
    }

    private ReportDtos.TopCategoryVolumeResponse toTopCategoryVolume(Object[] row) {
        // [categoryName, sum(quantity)]
        String categoryName = (String) row[0];
        long totalQuantity = toLong(row, 1);
        return new ReportDtos.TopCategoryVolumeResponse(categoryName, totalQuantity);
    }

    private ReportDtos.BestSellingProductResponse toBestSellingProduct(Object[] row) {
        // [product, sum(quantity)]
        Product product = (Product) row[0];
        long totalSold = toLong(row, 1);
        return new ReportDtos.BestSellingProductResponse(product.getId(), product.getSku(), product.getName(), totalSold);
    }

    private ReportDtos.LowStockProductResponse toLowStockProduct(Object[] row) {
        // [productId, name, sku, availableStock, minimumStock]
        UUID productId = (UUID) row[0];
        String name = (String) row[1];
        String sku = (String) row[2];
        int available = toInt(row, 3);
        int minimum = toInt(row, 4);
        return new ReportDtos.LowStockProductResponse(productId, name, sku, available, minimum);
    }

    private int toInt(Object[] row, int idx) {
        Object v = row[idx];
        if (v == null) return 0;
        if (v instanceof Integer i) return i;
        if (v instanceof Long l) return Math.toIntExact(l);
        if (v instanceof Short s) return s.intValue();
        if (v instanceof Number n) return n.intValue();
        throw new ValidationException("Expected number at index " + idx + " but got: " + v.getClass().getName());
    }

    private long toLong(Object[] row, int idx) {
        Object v = row[idx];
        if (v == null) return 0L;
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof Short s) return s.longValue();
        if (v instanceof Number n) return n.longValue();
        throw new ValidationException("Expected number at index " + idx + " but got: " + v.getClass().getName());
    }

    private BigDecimal toBigDecimal(Object[] row, int idx) {
        Object v = row[idx];
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        throw new ValidationException("Expected BigDecimal/Number at index " + idx + " but got: " + v.getClass().getName());
    }
}

