package com.yoot.flashcard.modules.content.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.content.dto.FlashcardRequest;
import com.yoot.flashcard.modules.content.dto.FlashcardResponse;
import com.yoot.flashcard.modules.content.service.FlashcardService;
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
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping("/api/v1/decks/{deckId}/flashcards")
    public ApiResponse<PageResponse<FlashcardResponse>> listFlashcards(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.success("Flashcards retrieved", flashcardService.listFlashcards(deckId, page, size));
    }

    @PostMapping("/api/v1/decks/{deckId}/flashcards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FlashcardResponse>> createFlashcard(
            @PathVariable Long deckId,
            @Valid @RequestBody FlashcardRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Flashcard created", flashcardService.createFlashcard(deckId, request)));
    }

    @PutMapping("/api/v1/flashcards/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FlashcardResponse> updateFlashcard(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardRequest request
    ) {
        return ApiResponse.success("Flashcard updated", flashcardService.updateFlashcard(id, request));
    }

    @DeleteMapping("/api/v1/flashcards/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteFlashcard(@PathVariable Long id) {
        flashcardService.deleteFlashcard(id);
        return ResponseEntity.noContent().build();
    }
}
