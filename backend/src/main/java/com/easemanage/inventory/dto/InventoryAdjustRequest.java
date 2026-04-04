package com.easemanage.inventory.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryAdjustRequest(
    @NotNull Long productId,
    @NotNull Long warehouseId,
    @NotNull Integer quantity,
    String reason
) {}
