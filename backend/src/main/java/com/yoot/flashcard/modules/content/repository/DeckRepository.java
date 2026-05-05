package com.yoot.flashcard.modules.content.repository;

import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.DeckStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface DeckRepository extends MongoRepository<Deck, Long> {

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatus(DeckStatus status);

    @Query("{ '_id': ?0, 'deletedAt': null }")
    Optional<Deck> findActiveById(Long id);
}
