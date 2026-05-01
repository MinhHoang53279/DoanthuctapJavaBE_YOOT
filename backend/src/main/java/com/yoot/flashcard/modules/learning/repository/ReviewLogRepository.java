package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.ReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewLogRepository extends JpaRepository<ReviewLog, Long> {
}
