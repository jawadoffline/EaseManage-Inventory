package com.easemanage.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank String name,
    String description,
    Long categoryId,
    String unitOfMeasure,
    @Min(0) Integer minStockLevel,
    Integer maxStockLevel,
    @Min(0) Integer reorderPoint,
    @DecimalMin("0.0") BigDecimal costPrice,
    @DecimalMin("0.0") BigDecimal sellingPrice,
    String barcode,
    String imageUrl,
    Boolean isActive
) {}
