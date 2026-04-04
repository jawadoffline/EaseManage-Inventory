package com.easemanage.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
    Long id,
    String name,
    String email,
    String phone,
    String address,
    String city,
    String country,
    String contactPerson,
    String notes,
    Boolean isActive,
    LocalDateTime createdAt
) {}
