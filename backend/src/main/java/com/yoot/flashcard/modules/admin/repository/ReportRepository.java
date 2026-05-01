package com.yoot.flashcard.modules.admin.repository;

import com.yoot.flashcard.modules.admin.entity.Report;
import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import com.yoot.flashcard.modules.admin.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReportRepository extends JpaRepository<Report, Long> {

    long countByStatus(ReportStatus status);

    @EntityGraph(attributePaths = "reporter")
    @Query("""
            select r from Report r
            where (:targetType is null or r.targetType = :targetType)
              and (:status is null or r.status = :status)
            """)
    Page<Report> search(
            @Param("targetType") ReportTargetType targetType,
            @Param("status") ReportStatus status,
            Pageable pageable
    );
}
