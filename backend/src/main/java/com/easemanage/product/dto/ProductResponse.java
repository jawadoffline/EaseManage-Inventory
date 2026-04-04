package com.easemanage.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    Long categoryId,
    String categoryName,
    String unitOfMeasure,
    Integer minStockLevel,
    Integer maxStockLevel,
    Integer reorderPoint,
    BigDecimal costPrice,
    BigDecimal sellingPrice,
    String barcode,
    String imageUrl,
    Boolean isActive,
    Integer totalStock,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
