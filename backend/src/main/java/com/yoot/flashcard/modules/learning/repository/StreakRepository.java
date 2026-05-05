package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.Streak;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface StreakRepository extends MongoRepository<Streak, Long> {

    @Query("{ 'user.$id': ?0 }")
    Optional<Streak> findByUserId(Long userId);
}
