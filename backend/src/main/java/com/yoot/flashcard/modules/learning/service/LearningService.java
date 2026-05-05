package com.yoot.flashcard.modules.learning.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.Flashcard;
import com.yoot.flashcard.modules.content.service.ContentAccessService;
import com.yoot.flashcard.modules.content.service.DeckService;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import com.yoot.flashcard.modules.learning.algorithm.ReviewSchedulingService;
import com.yoot.flashcard.modules.learning.dto.LearningOverviewResponse;
import com.yoot.flashcard.modules.learning.dto.LearningProgressResponse;
import com.yoot.flashcard.modules.learning.dto.ReviewResultResponse;
import com.yoot.flashcard.modules.learning.dto.StudyCardResponse;
import com.yoot.flashcard.modules.learning.dto.StudySessionResponse;
import com.yoot.flashcard.modules.learning.dto.SubmitReviewRequest;
import com.yoot.flashcard.modules.learning.entity.LearningProgress;
import com.yoot.flashcard.modules.learning.entity.MasteryLevel;
import com.yoot.flashcard.modules.learning.entity.ReviewItem;
import com.yoot.flashcard.modules.learning.entity.ReviewLog;
import com.yoot.flashcard.modules.learning.entity.Streak;
import com.yoot.flashcard.modules.learning.entity.StudySession;
import com.yoot.flashcard.modules.learning.entity.StudySessionStatus;
import com.yoot.flashcard.modules.learning.repository.LearningFlashcardRepository;
import com.yoot.flashcard.modules.learning.repository.LearningProgressRepository;
import com.yoot.flashcard.modules.learning.repository.ReviewItemRepository;
import com.yoot.flashcard.modules.learning.repository.ReviewLogRepository;
import com.yoot.flashcard.modules.learning.repository.StreakRepository;
import com.yoot.flashcard.modules.learning.repository.StudySessionRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class LearningService {

    private static final int DEFAULT_SESSION_LIMIT = 20;
    private static final int MAX_SESSION_LIMIT = 100;

    private final DeckService deckService;
    private final ContentAccessService accessService;
    private final UserRepository userRepository;
    private final LearningFlashcardRepository flashcardRepository;
    private final StudySessionRepository studySessionRepository;
    private final ReviewItemRepository reviewItemRepository;
    private final ReviewLogRepository reviewLogRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final StreakRepository streakRepository;
    private final ReviewSchedulingService reviewSchedulingService;
    private final LearningProgressService learningProgressService;
    private final StreakService streakService;
    private final Clock clock;

    public LearningService(
            DeckService deckService,
            ContentAccessService accessService,
            UserRepository userRepository,
            LearningFlashcardRepository flashcardRepository,
            StudySessionRepository studySessionRepository,
            ReviewItemRepository reviewItemRepository,
            ReviewLogRepository reviewLogRepository,
            LearningProgressRepository learningProgressRepository,
            StreakRepository streakRepository,
            ReviewSchedulingService reviewSchedulingService,
            LearningProgressService learningProgressService,
            StreakService streakService,
            Clock clock
    ) {
        this.deckService = deckService;
        this.accessService = accessService;
        this.userRepository = userRepository;
        this.flashcardRepository = flashcardRepository;
        this.studySessionRepository = studySessionRepository;
        this.reviewItemRepository = reviewItemRepository;
        this.reviewLogRepository = reviewLogRepository;
        this.learningProgressRepository = learningProgressRepository;
        this.streakRepository = streakRepository;
        this.reviewSchedulingService = reviewSchedulingService;
        this.learningProgressService = learningProgressService;
        this.streakService = streakService;
        this.clock = clock;
    }
    public StudySessionResponse startStudySession(Long deckId, Integer limit) {
        Long userId = accessService.requireCurrentUserId();
        Deck deck = deckService.findDeck(deckId);
        accessService.requireDeckRead(deck);
        User user = findUser(userId);
        LocalDateTime now = LocalDateTime.now(clock);

        List<Flashcard> activeCards = flashcardRepository.findActiveByDeckId(
                deckId,
                Sort.by(Sort.Direction.ASC, "cardOrder").and(Sort.by(Sort.Direction.ASC, "id"))
        );
        Map<Long, ReviewItem> reviewItemsByFlashcardId = reviewItemRepository.findByUserIdAndFlashcardIds(
                        userId,
                        activeCards.stream().map(Flashcard::getId).toList()
                ).stream()
                .collect(Collectors.toMap(item -> item.getFlashcard().getId(), Function.identity()));
        List<Flashcard> cards = activeCards.stream()
                .filter(card -> isDueOrNew(reviewItemsByFlashcardId.get(card.getId()), now))
                .sorted(studyCardComparator(reviewItemsByFlashcardId))
                .limit(normalizeLimit(limit))
                .toList();

        StudySession session = new StudySession();
        session.setUser(user);
        session.setDeck(deck);
        session.setStartedAt(now);
        session.setStatus(StudySessionStatus.IN_PROGRESS);
        session.setTotalCards(cards.size());
        studySessionRepository.save(session);

        return new StudySessionResponse(
                session.getId(),
                deck.getId(),
                cards.stream().map(card -> toStudyCard(userId, card)).toList()
        );
    }
    public List<StudyCardResponse> reviewsToday(Integer limit) {
        Long userId = accessService.requireCurrentUserId();
        LocalDateTime now = LocalDateTime.now(clock);
        return reviewItemRepository.findDueItems(userId, now).stream()
                .filter(item -> item.getFlashcard().isActive() && item.getFlashcard().getDeletedAt() == null)
                .limit(normalizeLimit(limit))
                .map(item -> toStudyCard(item.getFlashcard(), item.getMasteryLevel()))
                .toList();
    }
    public ReviewResultResponse submitReview(Long flashcardId, SubmitReviewRequest request) {
        Long userId = accessService.requireCurrentUserId();
        User user = findUser(userId);
        Flashcard flashcard = flashcardRepository.findActiveById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
        accessService.requireDeckRead(flashcard.getDeck());

        StudySession session = resolveSession(request.studySessionId(), userId, flashcard.getDeck().getId());
        ReviewItem reviewItem = reviewItemRepository.findByUserIdAndFlashcardId(userId, flashcardId)
                .orElseGet(() -> {
                    ReviewItem created = new ReviewItem();
                    created.setUser(user);
                    created.setFlashcard(flashcard);
                    return created;
                });

        reviewSchedulingService.apply(reviewItem, request.rating());
        reviewItemRepository.save(reviewItem);

        LocalDateTime reviewedAt = reviewItem.getLastReviewAt();
        ReviewLog reviewLog = new ReviewLog();
        reviewLog.setReviewItem(reviewItem);
        reviewLog.setStudySession(session);
        reviewLog.setRating(request.rating());
        reviewLog.setQualityScore(request.rating().score());
        reviewLog.setResponseTimeMs(request.responseTimeMs());
        reviewLog.setReviewedAt(reviewedAt);
        reviewLogRepository.save(reviewLog);

        if (session != null) {
            session.setReviewedCards(session.getReviewedCards() + 1);
            studySessionRepository.save(session);
        }

        learningProgressService.updateProgress(user, flashcard.getDeck(), reviewedAt);
        streakService.updateStreak(user);

        return new ReviewResultResponse(
                flashcard.getId(),
                reviewItem.getMasteryLevel(),
                reviewItem.getIntervalDays(),
                reviewItem.getNextReviewAt()
        );
    }
    public void finishStudySession(Long sessionId) {
        Long userId = accessService.requireCurrentUserId();
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
        if (session.getStatus() != StudySessionStatus.IN_PROGRESS) {
            throw new BusinessException(HttpStatus.CONFLICT, "Study session is already finished");
        }
        session.setStatus(StudySessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now(clock));
        studySessionRepository.save(session);
    }
    public LearningOverviewResponse myProgress() {
        Long userId = accessService.requireCurrentUserId();
        List<LearningProgressResponse> decks = learningProgressRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(learningProgressService::toResponse)
                .toList();
        Streak streak = streakRepository.findByUserId(userId).orElse(null);
        return new LearningOverviewResponse(
                decks,
                streak == null ? 0 : streak.getCurrentStreakDays(),
                streak == null ? 0 : streak.getBestStreakDays()
        );
    }
    public LearningProgressResponse progressByDeck(Long deckId) {
        Long userId = accessService.requireCurrentUserId();
        LearningProgress progress = learningProgressRepository.findByUserIdAndDeckId(userId, deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning progress not found"));
        return learningProgressService.toResponse(progress);
    }

    private StudySession resolveSession(Long sessionId, Long userId, Long deckId) {
        if (sessionId == null) {
            return null;
        }
        StudySession session = studySessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Study session not found"));
        if (session.getStatus() != StudySessionStatus.IN_PROGRESS) {
            throw new BusinessException(HttpStatus.CONFLICT, "Study session is already finished");
        }
        if (!session.getDeck().getId().equals(deckId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Flashcard does not belong to this study session");
        }
        return session;
    }

    private StudyCardResponse toStudyCard(Long userId, Flashcard flashcard) {
        MasteryLevel masteryLevel = reviewItemRepository.findByUserIdAndFlashcardId(userId, flashcard.getId())
                .map(ReviewItem::getMasteryLevel)
                .orElse(MasteryLevel.NEW);
        return toStudyCard(flashcard, masteryLevel);
    }

    private StudyCardResponse toStudyCard(Flashcard flashcard, MasteryLevel masteryLevel) {
        return new StudyCardResponse(
                flashcard.getId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                masteryLevel
        );
    }

    private User findUser(Long userId) {
        return userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private boolean isDueOrNew(ReviewItem reviewItem, LocalDateTime now) {
        return reviewItem == null
                || reviewItem.getNextReviewAt() == null
                || !reviewItem.getNextReviewAt().isAfter(now);
    }

    private Comparator<Flashcard> studyCardComparator(Map<Long, ReviewItem> reviewItemsByFlashcardId) {
        return Comparator
                .comparing((Flashcard card) -> reviewRank(reviewItemsByFlashcardId.get(card.getId())))
                .thenComparing(card -> nextReviewAt(reviewItemsByFlashcardId.get(card.getId())), Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Flashcard::getCardOrder)
                .thenComparing(Flashcard::getId);
    }

    private int reviewRank(ReviewItem reviewItem) {
        if (reviewItem == null || reviewItem.getNextReviewAt() == null) {
            return 1;
        }
        return 0;
    }

    private LocalDateTime nextReviewAt(ReviewItem reviewItem) {
        return reviewItem == null ? null : reviewItem.getNextReviewAt();
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_SESSION_LIMIT;
        }
        return Math.min(limit, MAX_SESSION_LIMIT);
    }
}
