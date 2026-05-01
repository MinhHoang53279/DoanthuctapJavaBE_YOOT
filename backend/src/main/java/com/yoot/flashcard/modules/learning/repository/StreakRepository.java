package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {

    Optional<Streak> findByUserId(Long userId);
}
