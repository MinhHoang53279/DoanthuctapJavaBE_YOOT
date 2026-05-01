package com.yoot.flashcard.modules.admin.dto;

import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import com.yoot.flashcard.modules.admin.entity.ReportTargetType;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt
) {
}
