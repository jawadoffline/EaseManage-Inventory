package com.easemanage.stockmovement.dto;

public record StockMovementRequest(
    Long productId,
    Long fromWarehouseId,
    Long toWarehouseId,
    Integer quantity,
    String movementType,
    String reason
) {}
