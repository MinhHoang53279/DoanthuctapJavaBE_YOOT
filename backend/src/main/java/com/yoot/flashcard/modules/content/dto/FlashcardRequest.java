package com.yoot.flashcard.modules.content.dto;

import com.yoot.flashcard.modules.content.entity.DifficultyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FlashcardRequest(
        @NotBlank(message = "Front text is required")
        @Size(max = 5000, message = "Front text must not exceed 5000 characters")
        String frontText,

        @NotBlank(message = "Back text is required")
        @Size(max = 5000, message = "Back text must not exceed 5000 characters")
        String backText,

        @Size(max = 255, message = "Pronunciation must not exceed 255 characters")
        String pronunciation,

        @Size(max = 5000, message = "Example sentence must not exceed 5000 characters")
        String exampleSentence,

        @Size(max = 5000, message = "Note must not exceed 5000 characters")
        String note,

        DifficultyLevel difficultyLevel,
        Integer cardOrder
) {
}
