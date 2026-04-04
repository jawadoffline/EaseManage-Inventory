package com.easemanage.audit.service;

import com.easemanage.audit.dto.AuditLogResponse;
import com.easemanage.audit.entity.AuditLog;
import com.easemanage.audit.repository.AuditLogRepository;
import com.easemanage.common.dto.PagedResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void log_createsAuditEntry() {
        // No authentication set -- should default to "system"
        SecurityContextHolder.clearContext();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.log("Product", 1L, "CREATE", null, "name=Widget");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("Product", saved.getEntityType());
        assertEquals(1L, saved.getEntityId());
        assertEquals("CREATE", saved.getAction());
        assertNull(saved.getOldValues());
        assertEquals("name=Widget", saved.getNewValues());
        assertEquals("system", saved.getUsername());
    }

    @Test
    void log_withAuthentication_usesAuthenticatedUser() {
        com.easemanage.user.entity.User user = com.easemanage.user.entity.User.builder()
                .id(5L).username("testuser").email("test@test.com")
                .firstName("Test").lastName("User")
                .role(com.easemanage.user.entity.Role.ADMIN)
                .passwordHash("hash").build();

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.log("Category", 2L, "UPDATE", "old", "new");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals(5L, saved.getUserId());
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void getAuditLogs_returnsPagedResponse() {
        AuditLog log = AuditLog.builder()
                .id(1L).userId(1L).username("admin")
                .entityType("Product").entityId(1L)
                .action("CREATE").newValues("name=Widget")
                .build();
        log.setCreatedAt(LocalDateTime.now());

        Page<AuditLog> page = new PageImpl<>(List.of(log), PageRequest.of(0, 10), 1);
        when(auditLogRepository.search(isNull(), isNull(), eq(""), any(Pageable.class))).thenReturn(page);

        PagedResponse<AuditLogResponse> result = auditService.getAuditLogs(0, 10, null, null, "");

        assertEquals(1, result.content().size());
        assertEquals("Product", result.content().get(0).entityType());
        assertEquals("CREATE", result.content().get(0).action());
        assertTrue(result.last());
    }

    @Test
    void log_deletionAction_recordsOldValues() {
        SecurityContextHolder.clearContext();

        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.log("Supplier", 3L, "DELETE", "name=OldSupplier", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertEquals("DELETE", saved.getAction());
        assertEquals("name=OldSupplier", saved.getOldValues());
        assertNull(saved.getNewValues());
    }
}
