package com.yoot.flashcard.modules.learning.service;

import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.learning.entity.Streak;
import com.yoot.flashcard.modules.learning.repository.StreakRepository;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;

@Service
public class StreakService {

    private final StreakRepository streakRepository;
    private final Clock clock;

    public StreakService(StreakRepository streakRepository, Clock clock) {
        this.streakRepository = streakRepository;
        this.clock = clock;
    }

    public Streak updateStreak(User user) {
        LocalDate today = LocalDate.now(clock);
        Streak streak = streakRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Streak created = new Streak();
                    created.setUser(user);
                    return created;
                });

        LocalDate lastStudyDate = streak.getLastStudyDate();
        if (lastStudyDate == null) {
            streak.setCurrentStreakDays(1);
        } else if (lastStudyDate.equals(today)) {
            return streak;
        } else if (lastStudyDate.equals(today.minusDays(1))) {
            streak.setCurrentStreakDays(streak.getCurrentStreakDays() + 1);
        } else {
            streak.setCurrentStreakDays(1);
        }

        streak.setBestStreakDays(Math.max(streak.getBestStreakDays(), streak.getCurrentStreakDays()));
        streak.setLastStudyDate(today);
        return streakRepository.save(streak);
    }
}
