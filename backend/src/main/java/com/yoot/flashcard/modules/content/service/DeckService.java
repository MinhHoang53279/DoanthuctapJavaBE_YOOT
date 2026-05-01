package com.yoot.flashcard.modules.content.service;

import com.yoot.flashcard.common.exception.BusinessException;
import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.common.security.SecurityUtils;
import com.yoot.flashcard.modules.content.dto.DeckRequest;
import com.yoot.flashcard.modules.content.dto.DeckResponse;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.DeckStatus;
import com.yoot.flashcard.modules.content.entity.DeckVisibility;
import com.yoot.flashcard.modules.content.entity.Language;
import com.yoot.flashcard.modules.content.entity.Tag;
import com.yoot.flashcard.modules.content.entity.Topic;
import com.yoot.flashcard.modules.content.mapper.ContentMapper;
import com.yoot.flashcard.modules.content.repository.DeckRepository;
import com.yoot.flashcard.modules.content.repository.LanguageRepository;
import com.yoot.flashcard.modules.content.repository.TagRepository;
import com.yoot.flashcard.modules.content.repository.TopicRepository;
import com.yoot.flashcard.modules.identity.entity.User;
import com.yoot.flashcard.modules.identity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class DeckService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final DeckRepository deckRepository;
    private final LanguageRepository languageRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ContentMapper contentMapper;
    private final ContentAccessService accessService;

    public DeckService(
            DeckRepository deckRepository,
            LanguageRepository languageRepository,
            TopicRepository topicRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            ContentMapper contentMapper,
            ContentAccessService accessService
    ) {
        this.deckRepository = deckRepository;
        this.languageRepository = languageRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.contentMapper = contentMapper;
        this.accessService = accessService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DeckResponse> listDecks(
            int page,
            int size,
            String keyword,
            Long topicId,
            Long languageId,
            DeckVisibility visibility
    ) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                normalizeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<DeckResponse> decks = deckRepository.searchDecks(
                SecurityUtils.currentUserId().orElse(null),
                accessService.canManageContent(),
                DeckVisibility.PUBLIC,
                DeckStatus.APPROVED,
                normalizeKeyword(keyword),
                topicId,
                languageId,
                visibility,
                pageable
        ).map(contentMapper::toDeck);
        return PageResponse.from(decks);
    }

    @Transactional(readOnly = true)
    public DeckResponse getDeck(Long id) {
        Deck deck = findDeck(id);
        accessService.requireDeckRead(deck);
        return contentMapper.toDeck(deck);
    }

    @Transactional
    public DeckResponse createDeck(DeckRequest request) {
        Long userId = accessService.requireCurrentUserId();
        DeckVisibility visibility = request.visibility() == null ? DeckVisibility.PRIVATE : request.visibility();
        if (visibility == DeckVisibility.PUBLIC && !accessService.canManageContent()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only content managers or admins can create public decks");
        }

        User currentUser = userRepository.findWithRolesById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Deck deck = new Deck();
        applyDeckRequest(deck, request);
        deck.setVisibility(visibility);
        deck.setStatus(DeckStatus.DRAFT);
        deck.setCreatedBy(currentUser);
        return contentMapper.toDeck(deckRepository.save(deck));
    }

    @Transactional
    public DeckResponse updateDeck(Long id, DeckRequest request) {
        Deck deck = findDeck(id);
        accessService.requireDeckManage(deck);

        DeckVisibility visibility = request.visibility() == null ? deck.getVisibility() : request.visibility();
        if (visibility == DeckVisibility.PUBLIC && !accessService.canManageContent()) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only content managers or admins can publish decks");
        }

        applyDeckRequest(deck, request);
        deck.setVisibility(visibility);
        if (deck.getStatus() == DeckStatus.REJECTED) {
            deck.setStatus(DeckStatus.DRAFT);
            deck.setRejectionReason(null);
        }
        return contentMapper.toDeck(deckRepository.save(deck));
    }

    @Transactional
    public DeckResponse submitForReview(Long id) {
        Deck deck = findDeck(id);
        accessService.requireDeckManage(deck);
        if (deck.getVisibility() != DeckVisibility.PUBLIC) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Only public decks can be submitted for review");
        }

        deck.setStatus(DeckStatus.PENDING);
        deck.setApprovedBy(null);
        deck.setApprovedAt(null);
        deck.setRejectionReason(null);
        return contentMapper.toDeck(deckRepository.save(deck));
    }

    @Transactional
    public void deleteDeck(Long id) {
        Deck deck = findDeck(id);
        accessService.requireDeckManage(deck);
        deck.setDeletedAt(LocalDateTime.now());
        deckRepository.save(deck);
    }

    public Deck findDeck(Long id) {
        return deckRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found"));
    }

    private void applyDeckRequest(Deck deck, DeckRequest request) {
        deck.setTitle(request.title().trim());
        deck.setDescription(request.description());
        deck.setSourceLanguage(resolveLanguage(request.sourceLanguageId()));
        deck.setTargetLanguage(resolveLanguage(request.targetLanguageId()));
        deck.setTopic(resolveTopic(request.topicId()));
        deck.setTags(resolveTags(request.tagIds()));
    }

    private Language resolveLanguage(Long id) {
        if (id == null) {
            return null;
        }
        return languageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Language not found"));
    }

    private Topic resolveTopic(Long id) {
        if (id == null) {
            return null;
        }
        return topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found"));
    }

    private Set<Tag> resolveTags(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        Set<Tag> tags = new HashSet<>(tagRepository.findAllById(ids));
        if (tags.size() != ids.size()) {
            throw new ResourceNotFoundException("Tag not found");
        }
        return tags;
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
