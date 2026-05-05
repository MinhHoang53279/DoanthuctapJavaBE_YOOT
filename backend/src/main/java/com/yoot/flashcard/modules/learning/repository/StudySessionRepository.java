package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.StudySession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface StudySessionRepository extends MongoRepository<StudySession, Long> {

    @Query("{ '_id': ?0, 'user.$id': ?1 }")
    Optional<StudySession> findByIdAndUserId(Long id, Long userId);
}
