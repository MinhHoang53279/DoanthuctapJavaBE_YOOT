package com.yoot.flashcard.modules.learning.dto;

import com.yoot.flashcard.modules.learning.entity.ReviewRating;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record SubmitReviewRequest(
        Long studySessionId,

        @NotNull(message = "Rating is required")
        ReviewRating rating,

        @Min(value = 0, message = "Response time must be non-negative")
        Long responseTimeMs
) {
}
