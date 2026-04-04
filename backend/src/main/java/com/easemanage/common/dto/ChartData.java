package com.easemanage.common.dto;

import java.util.List;

public record ChartData(
    List<CategoryStockData> stockByCategory,
    List<OrderStatusData> orderStatus
) {
    public record CategoryStockData(String name, int stock) {}
    public record OrderStatusData(String name, int value) {}
}
