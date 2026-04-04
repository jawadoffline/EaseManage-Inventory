package com.easemanage.report.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryValuationReport(
    BigDecimal totalCostValue,
    BigDecimal totalRetailValue,
    int totalProducts,
    int totalUnits,
    List<ProductValuation> items
) {
    public record ProductValuation(
        Long productId, String productName, String sku, String categoryName,
        int quantity, BigDecimal costPrice, BigDecimal sellingPrice,
        BigDecimal totalCostValue, BigDecimal totalRetailValue
    ) {}
}
