package com.yoot.flashcard.modules.content.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "flashcards")
@SequencedDocument("flashcards")
public class Flashcard {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private Deck deck;

    private String frontText;

    private String backText;

    private String pronunciation;

    private String exampleSentence;

    private String note;

    private DifficultyLevel difficultyLevel;

    private int cardOrder;

    @Indexed
    private boolean active = true;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Indexed
    private LocalDateTime deletedAt;
}
