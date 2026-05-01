CREATE TABLE IF NOT EXISTS languages (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS topics (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS decks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NULL,
    source_language_id BIGINT NULL,
    target_language_id BIGINT NULL,
    topic_id BIGINT NULL,
    visibility VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    created_by BIGINT NOT NULL,
    approved_by BIGINT NULL,
    approved_at TIMESTAMP NULL,
    rejection_reason TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT chk_decks_visibility CHECK (visibility IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT chk_decks_status CHECK (status IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT fk_decks_source_language
        FOREIGN KEY (source_language_id) REFERENCES languages (id),
    CONSTRAINT fk_decks_target_language
        FOREIGN KEY (target_language_id) REFERENCES languages (id),
    CONSTRAINT fk_decks_topic
        FOREIGN KEY (topic_id) REFERENCES topics (id),
    CONSTRAINT fk_decks_created_by
        FOREIGN KEY (created_by) REFERENCES users (id),
    CONSTRAINT fk_decks_approved_by
        FOREIGN KEY (approved_by) REFERENCES users (id)
);

CREATE INDEX IF NOT EXISTS idx_decks_created_by ON decks (created_by);
CREATE INDEX IF NOT EXISTS idx_decks_visibility_status ON decks (visibility, status);
CREATE INDEX IF NOT EXISTS idx_decks_topic_id ON decks (topic_id);
CREATE INDEX IF NOT EXISTS idx_decks_target_language_id ON decks (target_language_id);

CREATE TABLE IF NOT EXISTS deck_tags (
    deck_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (deck_id, tag_id),
    CONSTRAINT fk_deck_tags_deck
        FOREIGN KEY (deck_id) REFERENCES decks (id),
    CONSTRAINT fk_deck_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE IF NOT EXISTS flashcards (
    id BIGSERIAL PRIMARY KEY,
    deck_id BIGINT NOT NULL,
    front_text TEXT NOT NULL,
    back_text TEXT NOT NULL,
    pronunciation VARCHAR(255) NULL,
    example_sentence TEXT NULL,
    note TEXT NULL,
    difficulty_level VARCHAR(30) NULL,
    card_order INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL,
    CONSTRAINT chk_flashcards_difficulty CHECK (
        difficulty_level IS NULL OR difficulty_level IN ('EASY', 'MEDIUM', 'HARD')
    ),
    CONSTRAINT fk_flashcards_deck
        FOREIGN KEY (deck_id) REFERENCES decks (id)
);

CREATE INDEX IF NOT EXISTS idx_flashcards_deck_id ON flashcards (deck_id);
CREATE INDEX IF NOT EXISTS idx_flashcards_active ON flashcards (active);

CREATE TABLE IF NOT EXISTS flashcard_media (
    id BIGSERIAL PRIMARY KEY,
    flashcard_id BIGINT NOT NULL,
    media_type VARCHAR(30) NOT NULL,
    media_url VARCHAR(500) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_flashcard_media_flashcard
        FOREIGN KEY (flashcard_id) REFERENCES flashcards (id)
);
