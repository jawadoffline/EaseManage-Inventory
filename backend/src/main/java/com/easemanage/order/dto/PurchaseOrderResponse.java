package com.easemanage.order.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PurchaseOrderResponse(
    Long id,
    String orderNumber,
    Long supplierId,
    String supplierName,
    Long warehouseId,
    String warehouseName,
    String status,
    BigDecimal totalAmount,
    LocalDate expectedDelivery,
    String notes,
    String createdByName,
    List<OrderItemResponse> items,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public record OrderItemResponse(
        Long id, Long productId, String productName, String productSku,
        Integer quantity, BigDecimal unitPrice, Integer receivedQuantity
    ) {}
}
