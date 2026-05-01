package com.yoot.flashcard.modules.content.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.modules.content.dto.LanguageRequest;
import com.yoot.flashcard.modules.content.dto.LanguageResponse;
import com.yoot.flashcard.modules.content.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/languages")
public class LanguageController {

    private final CatalogService catalogService;

    public LanguageController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<List<LanguageResponse>> listLanguages() {
        return ApiResponse.success("Languages retrieved", catalogService.listLanguages());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<LanguageResponse>> createLanguage(@Valid @RequestBody LanguageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Language created", catalogService.createLanguage(request)));
    }
}
