package com.yoot.flashcard.modules.identity.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 150, message = "Full name must not exceed 150 characters")
        String fullName,

        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        String avatarUrl,

        @Size(max = 10, message = "Native language code must not exceed 10 characters")
        String nativeLanguageCode,

        @Size(max = 10, message = "Target language code must not exceed 10 characters")
        String targetLanguageCode,

        @Size(max = 50, message = "Timezone must not exceed 50 characters")
        String timezone,

        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        String bio
) {
}
