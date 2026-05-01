package com.yoot.flashcard.modules.admin.repository;

import com.yoot.flashcard.modules.admin.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "actor")
    @Query("""
            select a from AuditLog a
            where (:action is null or a.action = :action)
              and (:resourceType is null or a.resourceType = :resourceType)
              and (:resourceId is null or a.resourceId = :resourceId)
            """)
    Page<AuditLog> search(
            @Param("action") String action,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId,
            Pageable pageable
    );
}
