package com.yoot.flashcard.modules.learning.dto;

import com.yoot.flashcard.modules.learning.entity.MasteryLevel;

public record StudyCardResponse(
        Long flashcardId,
        String frontText,
        String backText,
        MasteryLevel masteryLevel
) {
}
