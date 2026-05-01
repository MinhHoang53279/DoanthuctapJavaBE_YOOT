package com.yoot.flashcard.modules.content.dto;

public record LanguageResponse(
        Long id,
        String code,
        String name,
        boolean active
) {
}
