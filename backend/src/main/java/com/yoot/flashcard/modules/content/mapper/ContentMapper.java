package com.yoot.flashcard.modules.content.mapper;

import com.yoot.flashcard.modules.content.dto.DeckResponse;
import com.yoot.flashcard.modules.content.dto.FlashcardResponse;
import com.yoot.flashcard.modules.content.dto.LanguageResponse;
import com.yoot.flashcard.modules.content.dto.TagResponse;
import com.yoot.flashcard.modules.content.dto.TopicResponse;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.content.entity.Flashcard;
import com.yoot.flashcard.modules.content.entity.Language;
import com.yoot.flashcard.modules.content.entity.Tag;
import com.yoot.flashcard.modules.content.entity.Topic;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class ContentMapper {

    public LanguageResponse toLanguage(Language language) {
        if (language == null) {
            return null;
        }
        return new LanguageResponse(language.getId(), language.getCode(), language.getName(), language.isActive());
    }

    public TopicResponse toTopic(Topic topic) {
        if (topic == null) {
            return null;
        }
        return new TopicResponse(topic.getId(), topic.getName(), topic.getDescription(), topic.isActive());
    }

    public TagResponse toTag(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName());
    }

    public DeckResponse toDeck(Deck deck) {
        Long approvedBy = deck.getApprovedBy() == null ? null : deck.getApprovedBy().getId();
        Set<TagResponse> tags = deck.getTags().stream()
                .map(this::toTag)
                .collect(Collectors.toCollection(() -> new TreeSet<>((left, right) -> left.name().compareToIgnoreCase(right.name()))));

        return new DeckResponse(
                deck.getId(),
                deck.getTitle(),
                deck.getDescription(),
                toLanguage(deck.getSourceLanguage()),
                toLanguage(deck.getTargetLanguage()),
                toTopic(deck.getTopic()),
                deck.getVisibility(),
                deck.getStatus(),
                deck.getCreatedBy().getId(),
                approvedBy,
                deck.getApprovedAt(),
                deck.getRejectionReason(),
                tags,
                deck.getCreatedAt(),
                deck.getUpdatedAt()
        );
    }

    public FlashcardResponse toFlashcard(Flashcard flashcard) {
        return new FlashcardResponse(
                flashcard.getId(),
                flashcard.getDeck().getId(),
                flashcard.getFrontText(),
                flashcard.getBackText(),
                flashcard.getPronunciation(),
                flashcard.getExampleSentence(),
                flashcard.getNote(),
                flashcard.getDifficultyLevel(),
                flashcard.getCardOrder(),
                flashcard.isActive(),
                flashcard.getCreatedAt(),
                flashcard.getUpdatedAt()
        );
    }
}
