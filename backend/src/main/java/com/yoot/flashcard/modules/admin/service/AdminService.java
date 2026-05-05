package com.yoot.flashcard.modules.admin.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.admin.dto.AdminDashboardResponse;
import com.yoot.flashcard.modules.admin.dto.DeckRejectionRequest;
import com.yoot.flashcard.modules.admin.entity.ReportStatus;
import com.yoot.flashcard.modules.admin.repository.ReportRepository;
import com.yoot.flashcard.modules.content.dto.DeckResponse;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.DeckStatus;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import com.yoot.flashcard.modules.content.mapper.ContentMapper;
import com.yoot.flashcard.modules.content.repository.DeckRepository;
import com.yoot.flashcard.modules.content.repository.FlashcardRepository;
import com.yoot.flashcard.modules.identity.dto.UserDetailResponse;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.entity.UserStatus;
import com.yoot.flashcard.modules.identity.mapper.UserMapper;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import com.yoot.flashcard.modules.learning.repository.StudySessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final DeckRepository deckRepository;
    private final FlashcardRepository flashcardRepository;
    private final StudySessionRepository studySessionRepository;
    private final ReportRepository reportRepository;
    private final AuditLogService auditLogService;
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final Clock clock;

    public AdminService(
            UserRepository userRepository,
            DeckRepository deckRepository,
            FlashcardRepository flashcardRepository,
            StudySessionRepository studySessionRepository,
            ReportRepository reportRepository,
            AuditLogService auditLogService,
            ContentMapper contentMapper,
            UserMapper userMapper,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.deckRepository = deckRepository;
        this.flashcardRepository = flashcardRepository;
        this.studySessionRepository = studySessionRepository;
        this.reportRepository = reportRepository;
        this.auditLogService = auditLogService;
        this.contentMapper = contentMapper;
        this.userMapper = userMapper;
        this.clock = clock;
    }
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                userRepository.countByDeletedAtIsNull(),
                userRepository.countByDeletedAtIsNullAndStatus(UserStatus.ACTIVE),
                deckRepository.countByDeletedAtIsNull(),
                deckRepository.countByDeletedAtIsNullAndStatus(DeckStatus.PENDING),
                deckRepository.countByDeletedAtIsNullAndStatus(DeckStatus.APPROVED),
                flashcardRepository.countByDeletedAtIsNullAndActiveTrue(),
                studySessionRepository.count(),
                reportRepository.countByStatus(ReportStatus.OPEN)
        );
    }
    public DeckResponse approveDeck(Long deckId) {
        User actor = requireCurrentUser();
        Deck deck = findDeckForModeration(deckId);
        DeckStatus previousStatus = deck.getStatus();

        deck.setStatus(DeckStatus.APPROVED);
        deck.setApprovedBy(actor);
        deck.setApprovedAt(LocalDateTime.now(clock));
        deck.setRejectionReason(null);
        Deck saved = deckRepository.save(deck);

        auditLogService.record(
                actor,
                "DECK_APPROVED",
                "DECK",
                saved.getId(),
                "status=%s->%s".formatted(previousStatus, DeckStatus.APPROVED)
        );
        return contentMapper.toDeck(saved);
    }
    public DeckResponse rejectDeck(Long deckId, DeckRejectionRequest request) {
        User actor = requireCurrentUser();
        Deck deck = findDeckForModeration(deckId);
        DeckStatus previousStatus = deck.getStatus();
        String reason = request.reason().trim();

        deck.setStatus(DeckStatus.REJECTED);
        deck.setApprovedBy(null);
        deck.setApprovedAt(null);
        deck.setRejectionReason(reason);
        Deck saved = deckRepository.save(deck);

        auditLogService.record(
                actor,
                "DECK_REJECTED",
                "DECK",
                saved.getId(),
                "status=%s->%s,reason=%s".formatted(previousStatus, DeckStatus.REJECTED, reason)
        );
        return contentMapper.toDeck(saved);
    }
    public UserDetailResponse lockUser(Long userId) {
        return changeUserStatus(userId, UserStatus.LOCKED, "USER_LOCKED");
    }
    public UserDetailResponse unlockUser(Long userId) {
        return changeUserStatus(userId, UserStatus.ACTIVE, "USER_UNLOCKED");
    }

    private UserDetailResponse changeUserStatus(Long userId, UserStatus newStatus, String action) {
        User actor = requireCurrentUser();
        if (actor.getId().equals(userId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Admins cannot change their own lock status");
        }

        User target = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        UserStatus previousStatus = target.getStatus();
        target.setStatus(newStatus);
        User saved = userRepository.save(target);

        auditLogService.record(
                actor,
                action,
                "USER",
                saved.getId(),
                "status=%s->%s".formatted(previousStatus, newStatus)
        );
        return userMapper.toDetail(saved);
    }

    private Deck findDeckForModeration(Long deckId) {
        Deck deck = deckRepository.findActiveById(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
        if (deck.getVisibility() != DeckVisibility.PUBLIC) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only public decks can be moderated");
        }
        if (deck.getStatus() != DeckStatus.PENDING) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only pending decks can be moderated");
        }
        return deck;
    }

    private User requireCurrentUser() {
        Long actorId = SecurityUtils.currentUserId()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        return userRepository.findWithRolesById(actorId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }
}
