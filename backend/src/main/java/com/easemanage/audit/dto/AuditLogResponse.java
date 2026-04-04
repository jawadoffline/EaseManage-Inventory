package com.easemanage.audit.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
    Long id,
    Long userId,
    String username,
    String entityType,
    Long entityId,
    String action,
    String oldValues,
    String newValues,
    String ipAddress,
    LocalDateTime createdAt
) {}
