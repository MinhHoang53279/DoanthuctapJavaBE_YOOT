package com.yoot.flashcard.modules.content.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.modules.content.dto.TagRequest;
import com.yoot.flashcard.modules.content.dto.TagResponse;
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
@RequestMapping("/api/v1/tags")
public class TagController {

    private final CatalogService catalogService;

    public TagController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<List<TagResponse>> listTags() {
        return ApiResponse.success("Tags retrieved", catalogService.listTags());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tag created", catalogService.createTag(request)));
    }
}
