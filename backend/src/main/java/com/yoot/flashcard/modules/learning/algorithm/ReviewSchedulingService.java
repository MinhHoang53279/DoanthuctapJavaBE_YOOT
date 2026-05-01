package com.yoot.flashcard.modules.learning.algorithm;

import com.yoot.flashcard.modules.learning.entity.MasteryLevel;
import com.yoot.flashcard.modules.learning.entity.ReviewItem;
import com.yoot.flashcard.modules.learning.entity.ReviewRating;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ReviewSchedulingService {

    private static final BigDecimal MIN_EASE_FACTOR = BigDecimal.valueOf(1.30);
    private static final BigDecimal HARD_EASE_PENALTY = BigDecimal.valueOf(0.15);
    private static final BigDecimal AGAIN_EASE_PENALTY = BigDecimal.valueOf(0.20);
    private static final BigDecimal GOOD_EASE_BONUS = BigDecimal.valueOf(0.05);
    private static final BigDecimal EASY_EASE_BONUS = BigDecimal.valueOf(0.15);

    private final Clock clock;

    public ReviewSchedulingService(Clock clock) {
        this.clock = clock;
    }

    public void apply(ReviewItem item, ReviewRating rating) {
        LocalDateTime now = LocalDateTime.now(clock);
        switch (rating) {
            case AGAIN -> applyAgain(item, now);
            case HARD -> applyHard(item, now);
            case GOOD -> applyGood(item, now);
            case EASY -> applyEasy(item, now);
        }

        item.setLastReviewAt(now);
        item.setMasteryLevel(resolveMasteryLevel(item));
    }

    private void applyAgain(ReviewItem item, LocalDateTime now) {
        item.setRepetitionCount(0);
        item.setIntervalDays(0);
        item.setEaseFactor(decreaseEase(item.getEaseFactor(), AGAIN_EASE_PENALTY));
        item.setNextReviewAt(now.plusMinutes(10));
        item.setMasteryLevel(MasteryLevel.LEARNING);
    }

    private void applyHard(ReviewItem item, LocalDateTime now) {
        item.setRepetitionCount(item.getRepetitionCount() + 1);
        int nextInterval = Math.max(1, Math.round(item.getIntervalDays() * 1.2f));
        item.setIntervalDays(nextInterval);
        item.setEaseFactor(decreaseEase(item.getEaseFactor(), HARD_EASE_PENALTY));
        item.setNextReviewAt(now.plusDays(nextInterval));
    }

    private void applyGood(ReviewItem item, LocalDateTime now) {
        item.setRepetitionCount(item.getRepetitionCount() + 1);
        int nextInterval;
        if (item.getRepetitionCount() == 1) {
            nextInterval = 1;
        } else if (item.getRepetitionCount() == 2) {
            nextInterval = 3;
        } else {
            nextInterval = Math.max(1, item.getEaseFactor().multiply(BigDecimal.valueOf(item.getIntervalDays()))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue());
        }
        item.setIntervalDays(nextInterval);
        item.setEaseFactor(item.getEaseFactor().add(GOOD_EASE_BONUS).setScale(2, RoundingMode.HALF_UP));
        item.setNextReviewAt(now.plusDays(nextInterval));
    }

    private void applyEasy(ReviewItem item, LocalDateTime now) {
        boolean newCard = item.getRepetitionCount() == 0;
        item.setRepetitionCount(item.getRepetitionCount() + 1);
        int nextInterval = newCard ? 4 : Math.max(1, item.getEaseFactor()
                .multiply(BigDecimal.valueOf(item.getIntervalDays()))
                .multiply(BigDecimal.valueOf(1.3))
                .setScale(0, RoundingMode.HALF_UP)
                .intValue());
        item.setIntervalDays(nextInterval);
        item.setEaseFactor(item.getEaseFactor().add(EASY_EASE_BONUS).setScale(2, RoundingMode.HALF_UP));
        item.setNextReviewAt(now.plusDays(nextInterval));
    }

    private BigDecimal decreaseEase(BigDecimal easeFactor, BigDecimal penalty) {
        BigDecimal value = easeFactor.subtract(penalty).setScale(2, RoundingMode.HALF_UP);
        return value.max(MIN_EASE_FACTOR);
    }

    private MasteryLevel resolveMasteryLevel(ReviewItem item) {
        if (item.getIntervalDays() >= 30 && item.getRepetitionCount() >= 5) {
            return MasteryLevel.MASTERED;
        }
        if (item.getIntervalDays() >= 7) {
            return MasteryLevel.REVIEWING;
        }
        return MasteryLevel.LEARNING;
    }
}
