package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.MasteryLevel;
import com.yoot.flashcard.modules.learning.entity.ReviewItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewItemRepository extends JpaRepository<ReviewItem, Long> {

    Optional<ReviewItem> findByUserIdAndFlashcardId(Long userId, Long flashcardId);

    @EntityGraph(attributePaths = {"flashcard", "flashcard.deck"})
    @Query("""
            select ri from ReviewItem ri
            where ri.user.id = :userId
              and ri.flashcard.active = true
              and ri.flashcard.deletedAt is null
              and ri.nextReviewAt <= :now
            order by ri.nextReviewAt asc
            """)
    List<ReviewItem> findDueItems(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("""
            select count(ri) from ReviewItem ri
            where ri.user.id = :userId
              and ri.flashcard.deck.id = :deckId
              and ri.repetitionCount > 0
            """)
    long countLearned(@Param("userId") Long userId, @Param("deckId") Long deckId);

    @Query("""
            select count(ri) from ReviewItem ri
            where ri.user.id = :userId
              and ri.flashcard.deck.id = :deckId
              and ri.masteryLevel = :masteryLevel
            """)
    long countMastered(
            @Param("userId") Long userId,
            @Param("deckId") Long deckId,
            @Param("masteryLevel") MasteryLevel masteryLevel
    );
}
