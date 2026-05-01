package com.yoot.flashcard.modules.identity.dto;

import com.yoot.flashcard.modules.identity.entity.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserDetailResponse(
        Long id,
        String email,
        String username,
        UserStatus status,
        Set<String> roles,
        UserProfileResponse profile,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
