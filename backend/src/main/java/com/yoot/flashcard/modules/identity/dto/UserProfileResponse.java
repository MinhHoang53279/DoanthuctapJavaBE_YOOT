package com.yoot.flashcard.modules.identity.dto;

public record UserProfileResponse(
        Long id,
        String fullName,
        String avatarUrl,
        String nativeLanguageCode,
        String targetLanguageCode,
        String timezone,
        String bio
) {
}
