package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.content.entity.Flashcard;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LearningFlashcardRepository extends MongoRepository<Flashcard, Long> {

    @Query("{ '_id': ?0, 'deletedAt': null, 'active': true }")
    Optional<Flashcard> findActiveById(Long id);

    @Query("{ 'deck.$id': ?0, 'active': true, 'deletedAt': null }")
    List<Flashcard> findActiveByDeckId(Long deckId, Sort sort);

    @Query(value = "{ 'deck.$id': ?0, 'active': true, 'deletedAt': null }", count = true)
    long countActiveByDeckId(Long deckId);
}
