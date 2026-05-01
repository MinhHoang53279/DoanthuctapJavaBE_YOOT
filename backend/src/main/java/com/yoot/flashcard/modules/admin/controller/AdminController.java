package com.yoot.flashcard.modules.admin.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.admin.dto.AdminDashboardResponse;
import com.yoot.flashcard.modules.admin.dto.AuditLogResponse;
import com.yoot.flashcard.modules.admin.dto.DeckRejectionRequest;
import com.yoot.flashcard.modules.admin.dto.ReportResponse;
import com.yoot.flashcard.modules.admin.dto.UpdateReportStatusRequest;
import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import com.yoot.flashcard.modules.admin.entity.ReportTargetType;
import com.yoot.flashcard.modules.admin.service.AdminService;
import com.yoot.flashcard.modules.admin.service.AuditLogService;
import com.yoot.flashcard.modules.admin.service.ReportService;
import com.yoot.flashcard.modules.content.dto.DeckResponse;
import com.yoot.flashcard.modules.identity.dto.UserDetailResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final ReportService reportService;
    private final AuditLogService auditLogService;

    public AdminController(AdminService adminService, ReportService reportService, AuditLogService auditLogService) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/health")
    @PreAuthorize("hasAuthority('ADMIN_DASHBOARD_READ')")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Admin module is ready", Map.of("module", "admin"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAuthority('ADMIN_DASHBOARD_READ')")
    public ApiResponse<AdminDashboardResponse> dashboard() {
        return ApiResponse.success("Admin dashboard retrieved", adminService.dashboard());
    }

    @PostMapping("/decks/{id}/approve")
    @PreAuthorize("hasAuthority('DECK_APPROVE')")
    public ApiResponse<DeckResponse> approveDeck(@PathVariable Long id) {
        return ApiResponse.success("Deck approved", adminService.approveDeck(id));
    }

    @PostMapping("/decks/{id}/reject")
    @PreAuthorize("hasAuthority('DECK_APPROVE')")
    public ApiResponse<DeckResponse> rejectDeck(
            @PathVariable Long id,
            @Valid @RequestBody DeckRejectionRequest request
    ) {
        return ApiResponse.success("Deck rejected", adminService.rejectDeck(id, request));
    }

    @PostMapping("/users/{id}/lock")
    @PreAuthorize("hasAuthority('USER_MANAGE_STATUS')")
    public ApiResponse<UserDetailResponse> lockUser(@PathVariable Long id) {
        return ApiResponse.success("User locked", adminService.lockUser(id));
    }

    @PostMapping("/users/{id}/unlock")
    @PreAuthorize("hasAuthority('USER_MANAGE_STATUS')")
    public ApiResponse<UserDetailResponse> unlockUser(@PathVariable Long id) {
        return ApiResponse.success("User unlocked", adminService.unlockUser(id));
    }

    @GetMapping("/reports")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ApiResponse<PageResponse<ReportResponse>> listReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ReportTargetType targetType,
            @RequestParam(required = false) ReportStatus status
    ) {
        return ApiResponse.success("Reports retrieved", reportService.listReports(page, size, targetType, status));
    }

    @PatchMapping("/reports/{id}/status")
    @PreAuthorize("hasAuthority('REPORT_MANAGE')")
    public ApiResponse<ReportResponse> updateReportStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReportStatusRequest request
    ) {
        return ApiResponse.success("Report status updated", reportService.updateStatus(id, request));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ApiResponse<PageResponse<AuditLogResponse>> listAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String resourceType,
            @RequestParam(required = false) Long resourceId
    ) {
        return ApiResponse.success(
                "Audit logs retrieved",
                auditLogService.listAuditLogs(page, size, action, resourceType, resourceId)
        );
    }
}
