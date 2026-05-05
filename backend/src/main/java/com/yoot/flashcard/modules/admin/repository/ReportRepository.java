package com.yoot.flashcard.modules.admin.repository;

import com.yoot.flashcard.modules.admin.entity.Report;
import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReportRepository extends MongoRepository<Report, Long> {

    long countByStatus(ReportStatus status);
}
