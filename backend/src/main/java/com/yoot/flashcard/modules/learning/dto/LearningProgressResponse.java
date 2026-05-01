package com.yoot.flashcard.modules.learning.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LearningProgressResponse(
        Long deckId,
        int learnedCards,
        int masteredCards,
        BigDecimal completionRate,
        LocalDateTime lastStudiedAt
) {
}
