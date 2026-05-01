package com.yoot.flashcard.modules.learning.dto;

import com.yoot.flashcard.modules.learning.entity.MasteryLevel;

import java.time.LocalDateTime;

public record ReviewResultResponse(
        Long flashcardId,
        MasteryLevel masteryLevel,
        int intervalDays,
        LocalDateTime nextReviewAt
) {
}
