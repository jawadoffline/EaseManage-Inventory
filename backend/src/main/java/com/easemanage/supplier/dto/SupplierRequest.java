package com.easemanage.supplier.dto;

import jakarta.validation.constraints.NotBlank;

public record SupplierRequest(
    @NotBlank String name,
    String email,
    String phone,
    String address,
    String city,
    String country,
    String contactPerson,
    String paymentTerms,
    Boolean isActive
) {}
