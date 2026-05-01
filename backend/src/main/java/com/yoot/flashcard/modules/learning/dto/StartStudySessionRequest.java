package com.yoot.flashcard.modules.learning.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StartStudySessionRequest(
        @NotNull(message = "Deck id is required")
        Long deckId,

        @Min(value = 1, message = "Limit must be at least 1")
        @Max(value = 100, message = "Limit must not exceed 100")
        Integer limit
) {
}
