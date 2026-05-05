package com.yoot.flashcard.modules.admin.repository;

import com.yoot.flashcard.modules.admin.entity.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AuditLogRepository extends MongoRepository<AuditLog, Long> {
}
