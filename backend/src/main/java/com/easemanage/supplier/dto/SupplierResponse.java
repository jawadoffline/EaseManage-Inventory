package com.easemanage.supplier.dto;

import java.time.LocalDateTime;

public record SupplierResponse(
    Long id,
    String name,
    String email,
    String phone,
    String address,
    String city,
    String country,
    String contactPerson,
    String paymentTerms,
    Boolean isActive,
    LocalDateTime createdAt
) {}
