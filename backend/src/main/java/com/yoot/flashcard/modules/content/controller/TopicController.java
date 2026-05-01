package com.yoot.flashcard.modules.content.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.modules.content.dto.TopicRequest;
import com.yoot.flashcard.modules.content.dto.TopicResponse;
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
@RequestMapping("/api/v1/topics")
public class TopicController {

    private final CatalogService catalogService;

    public TopicController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public ApiResponse<List<TopicResponse>> listTopics() {
        return ApiResponse.success("Topics retrieved", catalogService.listTopics());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN') or hasRole('CONTENT_MANAGER')")
    public ResponseEntity<ApiResponse<TopicResponse>> createTopic(@Valid @RequestBody TopicRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Topic created", catalogService.createTopic(request)));
    }
}
