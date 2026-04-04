package com.easemanage.inventory.dto;

import java.time.LocalDateTime;

public record InventoryResponse(
    Long id,
    Long productId,
    String productName,
    String productSku,
    Long warehouseId,
    String warehouseName,
    String warehouseCode,
    Integer quantity,
    Integer reservedQuantity,
    Integer availableQuantity,
    Integer minStockLevel,
    Integer reorderPoint,
    boolean lowStock,
    LocalDateTime lastCountedAt
) {}
