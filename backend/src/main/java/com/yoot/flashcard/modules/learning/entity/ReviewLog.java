package com.yoot.flashcard.modules.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "review_logs")
public class ReviewLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_item_id", nullable = false)
    private ReviewItem reviewItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_session_id")
    private StudySession studySession;

    @Column(name = "quality_score", nullable = false)
    private int qualityScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReviewRating rating;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;
}
