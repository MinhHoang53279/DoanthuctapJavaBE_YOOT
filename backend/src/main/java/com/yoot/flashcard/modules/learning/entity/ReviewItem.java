package com.yoot.flashcard.modules.learning.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.content.entity.Flashcard;
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
@Document(collection = "review_items")
@SequencedDocument("review_items")
@CompoundIndex(name = "uq_review_items_user_flashcard", def = "{'user.$id': 1, 'flashcard.$id': 1}", unique = true)
public class ReviewItem {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private User user;

    @DBRef
    @Indexed
    private Flashcard flashcard;

    private BigDecimal easeFactor = BigDecimal.valueOf(2.50);

    private int intervalDays;

    private int repetitionCount;

    @Indexed
    private MasteryLevel masteryLevel = MasteryLevel.NEW;

    private LocalDateTime lastReviewAt;

    @Indexed
    private LocalDateTime nextReviewAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
