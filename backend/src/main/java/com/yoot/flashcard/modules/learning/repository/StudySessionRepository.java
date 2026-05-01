package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.StudySession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudySessionRepository extends JpaRepository<StudySession, Long> {

    @EntityGraph(attributePaths = {"user", "deck"})
    Optional<StudySession> findByIdAndUserId(Long id, Long userId);
}
