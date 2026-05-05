package com.yoot.flashcard.modules.learning.entity;

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
@Document(collection = "review_logs")
@SequencedDocument("review_logs")
public class ReviewLog {

    @Id
    private Long id;

    @DBRef
    @Indexed
    private ReviewItem reviewItem;

    @DBRef
    @Indexed
    private StudySession studySession;

    private int qualityScore;

    private ReviewRating rating;

    private Long responseTimeMs;

    @Indexed
    private LocalDateTime reviewedAt;
}
