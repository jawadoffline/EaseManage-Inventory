package com.easemanage.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record SalesOrderRequest(
    @NotBlank String customerName,
    @NotNull Long warehouseId,
    String shippingAddress,
    @NotEmpty List<OrderItemRequest> items
) {}
