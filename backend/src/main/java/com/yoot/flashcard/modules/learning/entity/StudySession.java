package com.yoot.flashcard.modules.learning.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.content.entity.Deck;
import com.yoot.flashcard.modules.identity.entity.User;
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
@Document(collection = "study_sessions")
@SequencedDocument("study_sessions")
public class StudySession {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private User user;

    @DBRef
    @Indexed
    private Deck deck;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;

    private int totalCards;

    private int reviewedCards;

    @Indexed
    private StudySessionStatus status = StudySessionStatus.IN_PROGRESS;
}
