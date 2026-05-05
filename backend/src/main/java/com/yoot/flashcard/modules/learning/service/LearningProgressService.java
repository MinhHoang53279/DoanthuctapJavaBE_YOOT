package com.yoot.flashcard.modules.learning.service;

import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.learning.dto.LearningProgressResponse;
import com.yoot.flashcard.modules.learning.entity.LearningProgress;
import com.yoot.flashcard.modules.learning.entity.MasteryLevel;
import com.yoot.flashcard.modules.learning.repository.LearningFlashcardRepository;
import com.yoot.flashcard.modules.learning.repository.LearningProgressRepository;
import com.yoot.flashcard.modules.learning.repository.ReviewItemRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LearningProgressService {

    private final LearningProgressRepository learningProgressRepository;
    private final LearningFlashcardRepository flashcardRepository;
    private final ReviewItemRepository reviewItemRepository;

    public LearningProgressService(
            LearningProgressRepository learningProgressRepository,
            LearningFlashcardRepository flashcardRepository,
            ReviewItemRepository reviewItemRepository
    ) {
        this.learningProgressRepository = learningProgressRepository;
        this.flashcardRepository = flashcardRepository;
        this.reviewItemRepository = reviewItemRepository;
    }

    public LearningProgress updateProgress(User user, Deck deck, LocalDateTime studiedAt) {
        LearningProgress progress = learningProgressRepository.findByUserIdAndDeckId(user.getId(), deck.getId())
                .orElseGet(() -> {
                    LearningProgress created = new LearningProgress();
                    created.setUser(user);
                    created.setDeck(deck);
                    return created;
                });

        long totalCards = flashcardRepository.countActiveByDeckId(deck.getId());
        List<com.yoot.flashcard.modules.learning.entity.ReviewItem> userReviewItems = reviewItemRepository.findByUserId(user.getId());
        int learnedCards = Math.toIntExact(userReviewItems.stream()
                .filter(item -> item.getFlashcard().getDeck().getId().equals(deck.getId()))
                .filter(item -> item.getRepetitionCount() > 0)
                .count());
        int masteredCards = Math.toIntExact(userReviewItems.stream()
                .filter(item -> item.getFlashcard().getDeck().getId().equals(deck.getId()))
                .filter(item -> item.getMasteryLevel() == MasteryLevel.MASTERED)
                .count());
        BigDecimal completionRate = totalCards == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(learnedCards)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalCards), 2, RoundingMode.HALF_UP);

        progress.setLearnedCards(learnedCards);
        progress.setMasteredCards(masteredCards);
        progress.setCompletionRate(completionRate);
        progress.setLastStudiedAt(studiedAt);
        return learningProgressRepository.save(progress);
    }

    public LearningProgressResponse toResponse(LearningProgress progress) {
        return new LearningProgressResponse(
                progress.getDeck().getId(),
                progress.getLearnedCards(),
                progress.getMasteredCards(),
                progress.getCompletionRate(),
                progress.getLastStudiedAt()
        );
    }
}
