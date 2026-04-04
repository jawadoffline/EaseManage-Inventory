package com.easemanage.supplier.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record SupplierProductRequest(
    @NotNull Long productId,
    String supplierSku,
    Integer leadTimeDays,
    BigDecimal unitCost
) {}
