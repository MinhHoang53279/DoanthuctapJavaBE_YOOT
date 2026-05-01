package com.yoot.flashcard.modules.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LanguageRequest(
        @NotBlank(message = "Language code is required")
        @Size(max = 10, message = "Language code must not exceed 10 characters")
        String code,

        @NotBlank(message = "Language name is required")
        @Size(max = 100, message = "Language name must not exceed 100 characters")
        String name
) {
}
