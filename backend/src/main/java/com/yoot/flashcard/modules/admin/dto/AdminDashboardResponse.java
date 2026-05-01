package com.yoot.flashcard.modules.admin.dto;

public record AdminDashboardResponse(
        long totalUsers,
        long activeUsers,
        long totalDecks,
        long pendingDecks,
        long approvedDecks,
        long totalFlashcards,
        long totalStudySessions,
        long openReports
) {
}
