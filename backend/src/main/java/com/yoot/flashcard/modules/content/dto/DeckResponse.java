package com.yoot.flashcard.modules.content.dto;

import com.yoot.flashcard.modules.content.entity.DeckStatus;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;

import java.time.LocalDateTime;
import java.util.Set;

public record DeckResponse(
        Long id,
        String title,
        String description,
        LanguageResponse sourceLanguage,
        LanguageResponse targetLanguage,
        TopicResponse topic,
        DeckVisibility visibility,
        DeckStatus status,
        Long createdBy,
        Long approvedBy,
        LocalDateTime approvedAt,
        String rejectionReason,
        Set<TagResponse> tags,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
