package com.easemanage.stockmovement.dto;

import java.time.LocalDateTime;

public record StockMovementResponse(
    Long id,
    Long productId,
    String productName,
    String productSku,
    Long fromWarehouseId,
    String fromWarehouseName,
    Long toWarehouseId,
    String toWarehouseName,
    Integer quantity,
    String movementType,
    String referenceType,
    Long referenceId,
    String reason,
    Long createdById,
    String createdByName,
    LocalDateTime createdAt
) {}
