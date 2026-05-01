package com.yoot.flashcard.modules.admin.dto;

import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateReportStatusRequest(
        @NotNull ReportStatus status
) {
}
