package com.yoot.flashcard.modules.content.dto;

import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DeckRequest(
        @NotBlank(message = "Deck title is required")
        @Size(max = 150, message = "Deck title must not exceed 150 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        Long sourceLanguageId,
        Long targetLanguageId,
        Long topicId,
        DeckVisibility visibility,
        Set<Long> tagIds
) {
}
