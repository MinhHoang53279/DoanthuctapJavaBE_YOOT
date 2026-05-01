package com.yoot.flashcard.modules.admin.dto;

import com.yoot.flashcard.modules.admin.entity.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateReportRequest(
        @NotNull ReportTargetType targetType,
        @NotNull Long targetId,
        @NotBlank @Size(max = 2000) String reason
) {
}
