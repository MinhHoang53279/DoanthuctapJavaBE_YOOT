package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Flashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface FlashcardRepository extends MongoRepository<Flashcard, Long> {

    long countByDeletedAtIsNullAndActiveTrue();

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<Flashcard> findActiveById(Long id);

    @Query("{ 'deck.$id': ?0, 'deletedAt': null, 'active': true }")
    Page<Flashcard> findActiveByDeckId(Long deckId, Pageable pageable);
}
