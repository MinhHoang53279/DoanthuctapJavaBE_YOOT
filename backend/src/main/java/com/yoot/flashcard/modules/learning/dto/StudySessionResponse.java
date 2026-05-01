package com.yoot.flashcard.modules.learning.dto;

import java.util.List;

public record StudySessionResponse(
        Long sessionId,
        Long deckId,
        List<StudyCardResponse> cards
) {
}
