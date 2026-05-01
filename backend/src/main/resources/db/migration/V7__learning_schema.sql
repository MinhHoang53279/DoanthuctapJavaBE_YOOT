CREATE TABLE IF NOT EXISTS study_sessions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP NULL,
    total_cards INT NOT NULL DEFAULT 0,
    reviewed_cards INT NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    CONSTRAINT chk_study_sessions_status CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'ABANDONED')),
    CONSTRAINT fk_study_sessions_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_study_sessions_deck
        FOREIGN KEY (deck_id) REFERENCES decks (id)
);

CREATE TABLE IF NOT EXISTS review_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    flashcard_id BIGINT NOT NULL,
    ease_factor DECIMAL(4,2) NOT NULL DEFAULT 2.50,
    interval_days INT NOT NULL DEFAULT 0,
    repetition_count INT NOT NULL DEFAULT 0,
    mastery_level VARCHAR(30) NOT NULL DEFAULT 'NEW',
    last_review_at TIMESTAMP NULL,
    next_review_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_review_items_mastery_level CHECK (mastery_level IN ('NEW', 'LEARNING', 'REVIEWING', 'MASTERED')),
    CONSTRAINT uq_review_items_user_flashcard UNIQUE (user_id, flashcard_id),
    CONSTRAINT fk_review_items_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_review_items_flashcard
        FOREIGN KEY (flashcard_id) REFERENCES flashcards (id)
);

CREATE INDEX IF NOT EXISTS idx_review_items_user_next_review ON review_items (user_id, next_review_at);
CREATE INDEX IF NOT EXISTS idx_review_items_flashcard_id ON review_items (flashcard_id);

CREATE TABLE IF NOT EXISTS review_logs (
    id BIGSERIAL PRIMARY KEY,
    review_item_id BIGINT NOT NULL,
    study_session_id BIGINT NULL,
    quality_score INT NOT NULL,
    rating VARCHAR(30) NOT NULL,
    response_time_ms BIGINT NULL,
    reviewed_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_review_logs_rating CHECK (rating IN ('AGAIN', 'HARD', 'GOOD', 'EASY')),
    CONSTRAINT fk_review_logs_item
        FOREIGN KEY (review_item_id) REFERENCES review_items (id),
    CONSTRAINT fk_review_logs_session
        FOREIGN KEY (study_session_id) REFERENCES study_sessions (id)
);

CREATE TABLE IF NOT EXISTS learning_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    deck_id BIGINT NOT NULL,
    learned_cards INT NOT NULL DEFAULT 0,
    mastered_cards INT NOT NULL DEFAULT 0,
    completion_rate DECIMAL(5,2) NOT NULL DEFAULT 0,
    last_studied_at TIMESTAMP NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_learning_progress_user_deck UNIQUE (user_id, deck_id),
    CONSTRAINT fk_learning_progress_user
        FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_learning_progress_deck
        FOREIGN KEY (deck_id) REFERENCES decks (id)
);

CREATE TABLE IF NOT EXISTS streaks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    current_streak_days INT NOT NULL DEFAULT 0,
    best_streak_days INT NOT NULL DEFAULT 0,
    last_study_date DATE NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_streaks_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);
