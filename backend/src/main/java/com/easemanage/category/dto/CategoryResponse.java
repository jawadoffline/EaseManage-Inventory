package com.easemanage.category.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    Long parentId,
    String parentName,
    LocalDateTime createdAt,
    List<CategoryResponse> children
) {}
