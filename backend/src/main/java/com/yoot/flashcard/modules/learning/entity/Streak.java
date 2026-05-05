package com.yoot.flashcard.modules.learning.entity;

import com.yoot.flashcard.common.mongo.SequencedDocument;
import com.yoot.flashcard.modules.identity.entity.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "streaks")
@SequencedDocument("streaks")
public class Streak {

    @Id
    private Long id;

    @DBRef
    @Indexed(unique = true)
    private User user;

    private int currentStreakDays;

    private int bestStreakDays;

    private LocalDate lastStudyDate;

    private LocalDateTime updatedAt;
}
