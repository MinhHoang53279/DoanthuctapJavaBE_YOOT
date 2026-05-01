package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.LearningProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends JpaRepository<LearningProgress, Long> {

    @EntityGraph(attributePaths = "deck")
    Optional<LearningProgress> findByUserIdAndDeckId(Long userId, Long deckId);

    @EntityGraph(attributePaths = "deck")
    List<LearningProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
