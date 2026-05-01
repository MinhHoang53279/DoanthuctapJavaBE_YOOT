package com.yoot.flashcard.modules.identity.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.identity.dto.UpdateUserStatusRequest;
import com.yoot.flashcard.modules.identity.dto.UserDetailResponse;
import com.yoot.flashcard.modules.identity.dto.UserSummaryResponse;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import com.yoot.flashcard.modules.identity.service.IdentityService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class IdentityController {

    private final IdentityService identityService;

    public IdentityController(IdentityService identityService) {
        this.identityService = identityService;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Identity module is ready", Map.of("module", "identity"));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<PageResponse<UserSummaryResponse>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserStatus status
    ) {
        return ApiResponse.success("Users retrieved", identityService.listUsers(page, size, keyword, status));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ApiResponse<UserDetailResponse> getUser(@PathVariable Long id) {
        return ApiResponse.success("User retrieved", identityService.getUser(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE_STATUS')")
    public ApiResponse<UserDetailResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return ApiResponse.success("User status updated", identityService.updateStatus(id, request.status()));
    }
}
