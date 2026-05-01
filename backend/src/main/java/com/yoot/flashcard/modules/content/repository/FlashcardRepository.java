package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    @EntityGraph(attributePaths = {"deck", "deck.createdBy"})
    @Query("select f from Flashcard f where f.id = :id and f.deletedAt is null")
    Optional<Flashcard> findActiveById(@Param("id") Long id);

    @Query("""
            select f from Flashcard f
            where f.deletedAt is null
              and f.active = true
              and f.deck.id = :deckId
            order by f.cardOrder asc, f.id asc
            """)
    Page<Flashcard> findActiveByDeckId(@Param("deckId") Long deckId, Pageable pageable);
}
