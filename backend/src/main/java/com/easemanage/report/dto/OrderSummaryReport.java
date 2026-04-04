package com.easemanage.report.dto;

import java.math.BigDecimal;
import java.util.Map;

public record OrderSummaryReport(
    int totalPurchaseOrders,
    BigDecimal totalPurchaseValue,
    Map<String, Integer> purchaseByStatus,
    int totalSalesOrders,
    BigDecimal totalSalesValue,
    Map<String, Integer> salesByStatus
) {}
