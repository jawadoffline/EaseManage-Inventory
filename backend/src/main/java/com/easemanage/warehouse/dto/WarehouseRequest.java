package com.easemanage.warehouse.dto;

import jakarta.validation.constraints.NotBlank;

public record WarehouseRequest(
    @NotBlank String name,
    @NotBlank String code,
    String address,
    String city,
    String state,
    String country,
    Integer capacity,
    Boolean isActive
) {}
