package com.yoot.flashcard.modules.content.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.identity.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "decks")
@SequencedDocument("decks")
public class Deck {

    @Id
    private Long id;

    private String title;

    private String description;

    @DBRef
    private Language sourceLanguage;

    @DBRef
    private Language targetLanguage;

    @DBRef
    private Topic topic;

    @Indexed
    private DeckVisibility visibility = DeckVisibility.PRIVATE;

    @Indexed
    private DeckStatus status = DeckStatus.DRAFT;

    @DBRef
    @Indexed
    private User createdBy;

    @DBRef
    private User approvedBy;

    private LocalDateTime approvedAt;

    private String rejectionReason;

    @DBRef
    private Set<Tag> tags = new HashSet<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Indexed
    private LocalDateTime deletedAt;
}
