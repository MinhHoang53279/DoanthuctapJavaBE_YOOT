package com.yoot.flashcard.modules.content.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.content.dto.DeckRequest;
import com.yoot.flashcard.modules.content.dto.DeckResponse;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import com.yoot.flashcard.modules.content.service.DeckService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/decks")
public class DeckController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public ApiResponse<PageResponse<DeckResponse>> listDecks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Long languageId,
            @RequestParam(required = false) DeckVisibility visibility
    ) {
        return ApiResponse.success("Decks retrieved", deckService.listDecks(page, size, keyword, topicId, languageId, visibility));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeckResponse> getDeck(@PathVariable Long id) {
        return ApiResponse.success("Deck retrieved", deckService.getDeck(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DeckResponse>> createDeck(@Valid @RequestBody DeckRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deck created", deckService.createDeck(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DeckResponse> updateDeck(@PathVariable Long id, @Valid @RequestBody DeckRequest request) {
        return ApiResponse.success("Deck updated", deckService.updateDeck(id, request));
    }

    @PostMapping("/{id}/submit-review")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<DeckResponse> submitForReview(@PathVariable Long id) {
        return ApiResponse.success("Deck submitted for review", deckService.submitForReview(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteDeck(@PathVariable Long id) {
        deckService.deleteDeck(id);
        return ResponseEntity.noContent().build();
    }
}
