package com.easemanage.user.dto;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String username,
    String email,
    String firstName,
    String lastName,
    String role,
    String status,
    String avatarUrl,
    LocalDateTime createdAt
) {}
