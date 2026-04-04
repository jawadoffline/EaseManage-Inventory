package com.easemanage.report.dto;

import java.util.List;

public record StockSummaryReport(
    int totalProducts,
    int inStockCount,
    int lowStockCount,
    int outOfStockCount,
    List<CategoryStock> byCategory
) {
    public record CategoryStock(String categoryName, int productCount, int totalQuantity) {}
}
