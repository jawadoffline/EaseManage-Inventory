package com.easemanage.customer.dto;

import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(
    @NotBlank String name,
    String email,
    String phone,
    String address,
    String city,
    String country,
    String contactPerson,
    String notes,
    Boolean isActive
) {}
