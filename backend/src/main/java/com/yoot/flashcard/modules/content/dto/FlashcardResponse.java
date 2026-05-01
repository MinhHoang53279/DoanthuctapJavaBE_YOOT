package com.yoot.flashcard.modules.content.dto;

import com.yoot.flashcard.modules.content.entity.DifficultyLevel;

import java.time.LocalDateTime;

public record FlashcardResponse(
        Long id,
        Long deckId,
        String frontText,
        String backText,
        String pronunciation,
        String exampleSentence,
        String note,
        DifficultyLevel difficultyLevel,
        int cardOrder,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
