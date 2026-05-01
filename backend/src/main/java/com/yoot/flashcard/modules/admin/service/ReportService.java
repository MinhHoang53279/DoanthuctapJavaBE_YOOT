package com.yoot.flashcard.modules.admin.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.admin.dto.CreateReportRequest;
import com.yoot.flashcard.modules.admin.dto.ReportResponse;
import com.yoot.flashcard.modules.admin.dto.UpdateReportStatusRequest;
import com.yoot.flashcard.modules.admin.entity.Report;
import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import com.yoot.flashcard.modules.admin.entity.ReportTargetType;
import com.yoot.flashcard.modules.admin.mapper.AdminMapper;
import com.yoot.flashcard.modules.admin.repository.ReportRepository;
import com.yoot.flashcard.modules.content.repository.DeckRepository;
import com.yoot.flashcard.modules.content.repository.FlashcardRepository;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class ReportService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final AdminMapper adminMapper;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public ReportService(
            ReportRepository reportRepository,
            UserRepository userRepository,
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            AdminMapper adminMapper,
            AuditLogService auditLogService,
            Clock clock
    ) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.adminMapper = adminMapper;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    public ReportResponse createReport(CreateReportRequest request) {
        User reporter = requireCurrentUser();
        validateTarget(request.targetType(), request.targetId());

        Report report = new Report();
        report.setReporter(reporter);
        report.setTargetType(request.targetType());
        report.setTargetId(request.targetId());
        report.setReason(request.reason().trim());
        report.setStatus(ReportStatus.OPEN);
        Report saved = reportRepository.save(report);

        auditLogService.record(
                reporter,
                "REPORT_CREATED",
                "REPORT",
                saved.getId(),
                "targetType=%s,targetId=%d".formatted(saved.getTargetType(), saved.getTargetId())
        );
        return adminMapper.toReport(saved);
    }

    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> listReports(int page, int size, ReportTargetType targetType, ReportStatus status) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<ReportResponse> reports = reportRepository.search(targetType, status, pageable)
                .map(adminMapper::toReport);
        return PageResponse.from(reports);
    }

    @Transactional
    public ReportResponse updateStatus(Long id, UpdateReportStatusRequest request) {
        User actor = requireCurrentUser();
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found"));
        ReportStatus previousStatus = report.getStatus();

        report.setStatus(request.status());
        report.setResolvedAt(request.status() == ReportStatus.OPEN ? null : LocalDateTime.now(clock));
        Report saved = reportRepository.save(report);

        auditLogService.record(
                actor,
                "REPORT_STATUS_UPDATED",
                "REPORT",
                saved.getId(),
                "status=%s->%s".formatted(previousStatus, saved.getStatus())
        );
        return adminMapper.toReport(saved);
    }

    private void validateTarget(ReportTargetType targetType, Long targetId) {
        if (targetId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid report target");
        }
        boolean exists = switch (targetType) {
            case DECK -> deckRepository.findActiveById(targetId).isPresent();
            case FLASHCARD -> flashcardRepository.findActiveById(targetId).isPresent();
        };
        if (!exists) {
            throw new ResourceNotFoundException("Report target not found");
        }
    }

    private User requireCurrentUser() {
        Long userId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
