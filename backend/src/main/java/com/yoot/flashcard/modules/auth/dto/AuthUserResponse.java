package com.yoot.flashcard.modules.auth.dto;

import com.yoot.flashcard.modules.identity.entity.UserStatus;

import java.util.Set;

public record AuthUserResponse(
        Long id,
        String email,
        String username,
        String fullName,
        UserStatus status,
        Set<String> roles
) {
}
