package com.easemanage.audit.repository;

import com.easemanage.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:entityType IS NULL OR a.entityType = :entityType) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(a.username) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.entityType) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<AuditLog> search(@Param("entityType") String entityType,
                          @Param("action") String action,
                          @Param("search") String search,
                          Pageable pageable);
}
