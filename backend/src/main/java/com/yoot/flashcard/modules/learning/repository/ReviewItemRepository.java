package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.ReviewItem;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReviewItemRepository extends MongoRepository<ReviewItem, Long> {

    @Query("{ 'user.$id': ?0, 'flashcard.$id': ?1 }")
    Optional<ReviewItem> findByUserIdAndFlashcardId(Long userId, Long flashcardId);

    @Query(value = "{ 'user.$id': ?0, 'nextReviewAt': { $lte: ?1 } }", sort = "{ 'nextReviewAt': 1 }")
    List<ReviewItem> findDueItems(Long userId, LocalDateTime now);

    @Query("{ 'user.$id': ?0 }")
    List<ReviewItem> findByUserId(Long userId);

    @Query("{ 'user.$id': ?0, 'flashcard.$id': { $in: ?1 } }")
    List<ReviewItem> findByUserIdAndFlashcardIds(Long userId, List<Long> flashcardIds);
}
