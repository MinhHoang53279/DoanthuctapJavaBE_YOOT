package com.yoot.flashcard.modules.learning.algorithm;

import com.yoot.flashcard.modules.learning.entity.MasteryLevel;
import com.yoot.flashcard.modules.learning.entity.ReviewItem;
import com.yoot.flashcard.modules.learning.entity.ReviewRating;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ReviewSchedulingServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-05-01T10:00:00Z"), ZoneOffset.UTC);
    private final ReviewSchedulingService service = new ReviewSchedulingService(fixedClock);

    @Test
    void newCardWithGoodSchedulesOneDay() {
        ReviewItem item = new ReviewItem();
        item.setEaseFactor(BigDecimal.valueOf(2.50));

        service.apply(item, ReviewRating.GOOD);

        assertThat(item.getRepetitionCount()).isEqualTo(1);
        assertThat(item.getIntervalDays()).isEqualTo(1);
        assertThat(item.getMasteryLevel()).isEqualTo(MasteryLevel.LEARNING);
        assertThat(item.getNextReviewAt()).isEqualTo(LocalDateTime.of(2026, 5, 2, 10, 0));
    }

    @Test
    void againResetsRepetitionAndSchedulesSoon() {
        ReviewItem item = new ReviewItem();
        item.setEaseFactor(BigDecimal.valueOf(2.50));
        item.setRepetitionCount(3);
        item.setIntervalDays(10);

        service.apply(item, ReviewRating.AGAIN);

        assertThat(item.getRepetitionCount()).isZero();
        assertThat(item.getIntervalDays()).isZero();
        assertThat(item.getEaseFactor()).isEqualByComparingTo("2.30");
        assertThat(item.getMasteryLevel()).isEqualTo(MasteryLevel.LEARNING);
        assertThat(item.getNextReviewAt()).isEqualTo(LocalDateTime.of(2026, 5, 1, 10, 10));
    }
}
