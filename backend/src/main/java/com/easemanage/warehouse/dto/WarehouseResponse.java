package com.easemanage.warehouse.dto;

import java.time.LocalDateTime;

public record WarehouseResponse(
    Long id,
    String name,
    String code,
    String address,
    String city,
    String state,
    String country,
    Integer capacity,
    Boolean isActive,
    LocalDateTime createdAt
) {}
