package com.yoot.flashcard.modules.learning.controller;

import com.yoot.flashcard.common.response.ApiResponse;
import com.yoot.flashcard.modules.learning.dto.LearningOverviewResponse;
import com.yoot.flashcard.modules.learning.dto.LearningProgressResponse;
import com.yoot.flashcard.modules.learning.dto.ReviewResultResponse;
import com.yoot.flashcard.modules.learning.dto.StartStudySessionRequest;
import com.yoot.flashcard.modules.learning.dto.StudyCardResponse;
import com.yoot.flashcard.modules.learning.dto.StudySessionResponse;
import com.yoot.flashcard.modules.learning.dto.SubmitReviewRequest;
import com.yoot.flashcard.modules.learning.service.LearningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class LearningController {

    private final LearningService learningService;

    public LearningController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/api/v1/learning/health")
    public ApiResponse<Map<String, String>> health() {
        return ApiResponse.success("Learning module is ready", Map.of("module", "learning"));
    }

    @PostMapping("/api/v1/study-sessions/start")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<StudySessionResponse> startStudySession(@Valid @RequestBody StartStudySessionRequest request) {
        return ApiResponse.success("Study session started", learningService.startStudySession(request.deckId(), request.limit()));
    }

    @PostMapping("/api/v1/study-sessions/{id}/finish")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> finishStudySession(@PathVariable Long id) {
        learningService.finishStudySession(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/reviews/today")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<StudyCardResponse>> reviewsToday(@RequestParam(defaultValue = "50") Integer limit) {
        return ApiResponse.success("Reviews retrieved", learningService.reviewsToday(limit));
    }

    @PostMapping("/api/v1/reviews/{flashcardId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<ReviewResultResponse> submitReview(
            @PathVariable Long flashcardId,
            @Valid @RequestBody SubmitReviewRequest request
    ) {
        return ApiResponse.success("Review submitted", learningService.submitReview(flashcardId, request));
    }

    @GetMapping("/api/v1/progress/me")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningOverviewResponse> myProgress() {
        return ApiResponse.success("Progress retrieved", learningService.myProgress());
    }

    @GetMapping("/api/v1/progress/decks/{deckId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<LearningProgressResponse> progressByDeck(@PathVariable Long deckId) {
        return ApiResponse.success("Deck progress retrieved", learningService.progressByDeck(deckId));
    }
}
