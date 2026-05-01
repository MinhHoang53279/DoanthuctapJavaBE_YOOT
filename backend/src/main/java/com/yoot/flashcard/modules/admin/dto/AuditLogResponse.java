package com.yoot.flashcard.modules.admin.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long actorId,
        String action,
        String resourceType,
        Long resourceId,
        String details,
        LocalDateTime createdAt
) {
}
