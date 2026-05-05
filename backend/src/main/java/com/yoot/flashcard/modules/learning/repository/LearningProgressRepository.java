package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.LearningProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LearningProgressRepository extends MongoRepository<LearningProgress, Long> {

    @Query("{ 'user.$id': ?0, 'deck.$id': ?1 }")
    Optional<LearningProgress> findByUserIdAndDeckId(Long userId, Long deckId);

    @Query(value = "{ 'user.$id': ?0 }", sort = "{ 'updatedAt': -1 }")
    List<LearningProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);
}
