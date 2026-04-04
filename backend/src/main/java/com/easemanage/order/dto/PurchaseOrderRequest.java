package com.easemanage.order.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record PurchaseOrderRequest(
    @NotNull Long supplierId,
    @NotNull Long warehouseId,
    LocalDate expectedDelivery,
    String notes,
    @NotEmpty List<OrderItemRequest> items
) {}
