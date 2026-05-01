package com.yoot.flashcard.modules.content.service;

import com.yoot.flashcard.common.exception.ResourceNotFoundException;
import com.yoot.flashcard.common.response.PageResponse;
import com.yoot.flashcard.modules.content.dto.FlashcardRequest;
import com.yoot.flashcard.modules.content.dto.FlashcardResponse;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.Flashcard;
import com.yoot.flashcard.modules.content.mapper.ContentMapper;
import com.yoot.flashcard.modules.content.repository.FlashcardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class FlashcardService {

    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;

    private final FlashcardRepository flashcardRepository;
    private final DeckService deckService;
    private final ContentAccessService accessService;
    private final ContentMapper contentMapper;

    public FlashcardService(
            FlashcardRepository flashcardRepository,
            DeckService deckService,
            ContentAccessService accessService,
            ContentMapper contentMapper
    ) {
        this.flashcardRepository = flashcardRepository;
        this.deckService = deckService;
        this.accessService = accessService;
        this.contentMapper = contentMapper;
    }

    @Transactional(readOnly = true)
    public PageResponse<FlashcardResponse> listFlashcards(Long deckId, int page, int size) {
        Deck deck = deckService.findDeck(deckId);
        accessService.requireDeckRead(deck);
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizeSize(size));
        Page<FlashcardResponse> flashcards = flashcardRepository.findActiveByDeckId(deckId, pageable)
                .map(contentMapper::toFlashcard);
        return PageResponse.from(flashcards);
    }

    @Transactional
    public FlashcardResponse createFlashcard(Long deckId, FlashcardRequest request) {
        Deck deck = deckService.findDeck(deckId);
        accessService.requireDeckManage(deck);

        Flashcard flashcard = new Flashcard();
        flashcard.setDeck(deck);
        applyRequest(flashcard, request);
        return contentMapper.toFlashcard(flashcardRepository.save(flashcard));
    }

    @Transactional
    public FlashcardResponse updateFlashcard(Long id, FlashcardRequest request) {
        Flashcard flashcard = findFlashcard(id);
        accessService.requireDeckManage(flashcard.getDeck());
        applyRequest(flashcard, request);
        return contentMapper.toFlashcard(flashcardRepository.save(flashcard));
    }

    @Transactional
    public void deleteFlashcard(Long id) {
        Flashcard flashcard = findFlashcard(id);
        accessService.requireDeckManage(flashcard.getDeck());
        flashcard.setActive(false);
        flashcard.setDeletedAt(LocalDateTime.now());
        flashcardRepository.save(flashcard);
    }

    private Flashcard findFlashcard(Long id) {
        return flashcardRepository.findActiveById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found"));
    }

    private void applyRequest(Flashcard flashcard, FlashcardRequest request) {
        flashcard.setFrontText(request.frontText().trim());
        flashcard.setBackText(request.backText().trim());
        flashcard.setPronunciation(request.pronunciation());
        flashcard.setExampleSentence(request.exampleSentence());
        flashcard.setNote(request.note());
        flashcard.setDifficultyLevel(request.difficultyLevel());
        flashcard.setCardOrder(request.cardOrder() == null ? 0 : request.cardOrder());
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
