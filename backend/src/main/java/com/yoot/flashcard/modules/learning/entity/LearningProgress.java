package com.yoot.flashcard.modules.learning.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.identity.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "learning_progress")
@SequencedDocument("learning_progress")
@CompoundIndex(name = "uq_learning_progress_user_deck", def = "{'user.$id': 1, 'deck.$id': 1}", unique = true)
public class LearningProgress {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private User user;

    @DBRef
    @Indexed
    private Deck deck;

    private int learnedCards;

    private int masteredCards;

    private BigDecimal completionRate = BigDecimal.ZERO;

    private LocalDateTime lastStudiedAt;

    private LocalDateTime updatedAt;
}
