package com.easemanage.common.dto;

public record DashboardStats(
    long totalProducts,
    long lowStockAlerts,
    long totalWarehouses,
    long totalCategories
) {}
