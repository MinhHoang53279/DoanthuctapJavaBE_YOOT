package com.yoot.flashcard.modules.admin.mapper;

import com.yoot.flashcard.modules.admin.dto.AuditLogResponse;
import com.yoot.flashcard.modules.admin.dto.ReportResponse;
import com.yoot.flashcard.modules.admin.entity.AuditLog;
import com.yoot.flashcard.modules.admin.entity.Report;
import org.springframework.stereotype.Component;

@Component
public class AdminMapper {

    public AuditLogResponse toAuditLog(AuditLog auditLog) {
        Long actorId = auditLog.getActor() == null ? null : auditLog.getActor().getId();
        return new AuditLogResponse(
                auditLog.getId(),
                actorId,
                auditLog.getAction(),
                auditLog.getResourceType(),
                auditLog.getResourceId(),
                auditLog.getDetails(),
                auditLog.getCreatedAt()
        );
    }

    public ReportResponse toReport(Report report) {
        Long reporterId = report.getReporter() == null ? null : report.getReporter().getId();
        return new ReportResponse(
                report.getId(),
                reporterId,
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}
