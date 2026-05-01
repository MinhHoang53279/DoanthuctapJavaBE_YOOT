package com.yoot.flashcard.modules.content.dto;

public record TopicResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
}
