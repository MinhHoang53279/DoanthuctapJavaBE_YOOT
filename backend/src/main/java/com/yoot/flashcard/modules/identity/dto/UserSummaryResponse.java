package com.yoot.flashcard.modules.identity.dto;

import com.yoot.flashcard.modules.identity.entity.UserStatus;

import java.util.Set;

public record UserSummaryResponse(
        Long id,
        String email,
        String username,
        UserStatus status,
        Set<String> roles
) {
}
