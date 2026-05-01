package com.yoot.flashcard.modules.admin.service;

import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.admin.dto.AuditLogResponse;
import com.yoot.flashcard.modules.admin.entity.AuditLog;
import com.yoot.flashcard.modules.admin.mapper.AdminMapper;
import com.yoot.flashcard.modules.admin.repository.AuditLogRepository;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AdminMapper adminMapper;

    public AuditLogService(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            AdminMapper adminMapper
    ) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.adminMapper = adminMapper;
    }

    @Transactional
    public void recordCurrentActor(String action, String resourceType, Long resourceId, String details) {
        User actor = SecurityUtils.currentUserId()
                .flatMap(userRepository::findById)
                .orElse(null);
        record(actor, action, resourceType, resourceId, details);
    }

    @Transactional
    public void record(User actor, String action, String resourceType, Long resourceId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setActor(actor);
        auditLog.setAction(action);
        auditLog.setResourceType(resourceType);
        auditLog.setResourceId(resourceId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> listAuditLogs(
            int page,
            int size,
            String action,
            String resourceType,
            Long resourceId
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<AuditLogResponse> auditLogs = auditLogRepository.search(
                normalize(action),
                normalize(resourceType),
                resourceId,
                pageable
        ).map(adminMapper::toAuditLog);
        return PageResponse.from(auditLogs);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
