package com.yoot.flashcard.modules.learning.repository;

import com.yoot.flashcard.modules.learning.entity.ReviewLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReviewLogRepository extends MongoRepository<ReviewLog, Long> {
}
