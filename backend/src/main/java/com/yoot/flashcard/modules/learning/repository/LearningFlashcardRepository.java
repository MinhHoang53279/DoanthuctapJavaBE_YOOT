package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.content.entity.Flashcard;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LearningFlashcardRepository extends JpaRepository<Flashcard, Long> {

    @EntityGraph(attributePaths = {"deck", "deck.createdBy"})
    @Query("select f from Flashcard f where f.id = :id and f.deletedAt is null and f.active = true")
    Optional<Flashcard> findActiveById(@Param("id") Long id);

    @EntityGraph(attributePaths = "deck")
    @Query("""
            select f from Flashcard f
            left join ReviewItem ri on ri.flashcard = f and ri.user.id = :userId
            where f.deck.id = :deckId
              and f.active = true
              and f.deletedAt is null
              and (ri.id is null or ri.nextReviewAt is null or ri.nextReviewAt <= :now)
            order by
              case
                when ri.nextReviewAt is not null and ri.nextReviewAt <= :now then 0
                when ri.id is null then 1
                else 2
              end,
              ri.nextReviewAt asc,
              f.cardOrder asc,
              f.id asc
            """)
    List<Flashcard> findCardsForStudySession(
            @Param("userId") Long userId,
            @Param("deckId") Long deckId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
            select count(f) from Flashcard f
            where f.deck.id = :deckId
              and f.active = true
              and f.deletedAt is null
            """)
    long countActiveByDeckId(@Param("deckId") Long deckId);
}
