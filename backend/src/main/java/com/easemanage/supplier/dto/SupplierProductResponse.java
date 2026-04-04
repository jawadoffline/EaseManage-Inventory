package com.easemanage.supplier.dto;

import java.math.BigDecimal;

public record SupplierProductResponse(
    Long id,
    Long supplierId,
    String supplierName,
    Long productId,
    String productName,
    String productSku,
    String supplierSku,
    Integer leadTimeDays,
    BigDecimal unitCost
) {}
