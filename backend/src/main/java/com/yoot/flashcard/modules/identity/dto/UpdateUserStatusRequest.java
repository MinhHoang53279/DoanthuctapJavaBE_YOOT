package com.yoot.flashcard.modules.identity.dto;

import com.yoot.flashcard.modules.identity.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateUserStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}
