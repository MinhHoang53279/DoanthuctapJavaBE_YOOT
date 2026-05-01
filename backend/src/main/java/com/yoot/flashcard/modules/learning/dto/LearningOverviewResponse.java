package com.yoot.flashcard.modules.learning.dto;

import java.util.List;

public record LearningOverviewResponse(
        List<LearningProgressResponse> decks,
        int currentStreakDays,
        int bestStreakDays
) {
}
