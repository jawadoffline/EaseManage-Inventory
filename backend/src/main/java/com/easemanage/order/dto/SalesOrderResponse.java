package com.easemanage.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SalesOrderResponse(
    Long id,
    String orderNumber,
    String customerName,
    Long warehouseId,
    String warehouseName,
    String status,
    BigDecimal totalAmount,
    String shippingAddress,
    String createdByName,
    List<PurchaseOrderResponse.OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
