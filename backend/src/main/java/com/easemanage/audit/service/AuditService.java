package com.easemanage.audit.service;

import com.easemanage.audit.dto.AuditLogResponse;
import com.easemanage.audit.entity.AuditLog;
import com.easemanage.audit.repository.AuditLogRepository;
import com.easemanage.common.dto.PagedResponse;
import com.easemanage.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void log(String entityType, Long entityId, String action, String oldValues, String newValues) {
        Long userId = null;
        String username = "system";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            userId = user.getId();
            username = user.getUsername();
        }

        auditLogRepository.save(AuditLog.builder()
            .userId(userId).username(username)
            .entityType(entityType).entityId(entityId)
            .action(action).oldValues(oldValues).newValues(newValues)
            .build());
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogResponse> getAuditLogs(int page, int size, String entityType, String action, String search) {
        Page<AuditLog> logs = auditLogRepository.search(entityType, action, search,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return new PagedResponse<>(
            logs.getContent().stream().map(this::toResponse).toList(),
            logs.getNumber(), logs.getSize(),
            logs.getTotalElements(), logs.getTotalPages(), logs.isLast()
        );
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(a.getId(), a.getUserId(), a.getUsername(),
            a.getEntityType(), a.getEntityId(), a.getAction(),
            a.getOldValues(), a.getNewValues(), a.getIpAddress(), a.getCreatedAt());
    }
}
