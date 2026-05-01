# Database Design

## Nguyen tac thiet ke

1. PostgreSQL la database chinh.
2. Flyway quan ly migration.
3. Khong dung `ddl-auto=update` cho moi truong that.
4. Moi bang nghiep vu nen co `created_at` va `updated_at` neu du lieu co the thay doi.
5. Cac bang lich su nhu `review_logs` va `audit_logs` nen append-only.
6. Dung soft delete cho entity co lien quan den lich su hoc.

## Kieu du lieu chung

| Nhom | Kieu de xuat |
|---|---|
| ID | BIGSERIAL hoac BIGINT generated identity |
| Time | TIMESTAMP WITH TIME ZONE neu can timezone, neu khong thi TIMESTAMP |
| Money | Khong can trong MVP |
| Percent | DECIMAL(5,2) |
| Enum | VARCHAR(30) |
| Long text | TEXT |

## Migration plan

| Migration | Noi dung |
|---|---|
| V1 | Identity schema |
| V2 | Auth refresh token schema |
| V3 | Content schema |
| V4 | Learning schema |
| V5 | Admin schema co ban |
| V6 | Assessment schema |
| V7 | Classroom schema |
| V8 | Seed base data |

## Identity schema

### `users`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| email | VARCHAR(255) | UNIQUE, NOT NULL |
| username | VARCHAR(100) | UNIQUE, NOT NULL |
| password_hash | VARCHAR(255) | NOT NULL |
| status | VARCHAR(30) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| deleted_at | TIMESTAMP | NULL |

Index de xuat:

1. `idx_users_email`
2. `idx_users_username`
3. `idx_users_status`

### `user_profiles`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | UNIQUE, FK users.id |
| full_name | VARCHAR(150) | NULL |
| avatar_url | VARCHAR(500) | NULL |
| native_language_code | VARCHAR(10) | NULL |
| target_language_code | VARCHAR(10) | NULL |
| timezone | VARCHAR(50) | NULL |
| bio | TEXT | NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

### `roles`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| name | VARCHAR(50) | UNIQUE, NOT NULL |
| description | VARCHAR(255) | NULL |

### `permissions`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| code | VARCHAR(100) | UNIQUE, NOT NULL |
| description | VARCHAR(255) | NULL |

### `user_roles`

| Column | Type | Constraint |
|---|---|---|
| user_id | BIGINT | PK, FK users.id |
| role_id | BIGINT | PK, FK roles.id |

### `role_permissions`

| Column | Type | Constraint |
|---|---|---|
| role_id | BIGINT | PK, FK roles.id |
| permission_id | BIGINT | PK, FK permissions.id |

## Auth schema

### `refresh_tokens`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users.id, NOT NULL |
| token_hash | VARCHAR(255) | UNIQUE, NOT NULL |
| expires_at | TIMESTAMP | NOT NULL |
| revoked | BOOLEAN | NOT NULL DEFAULT FALSE |
| revoked_at | TIMESTAMP | NULL |
| created_at | TIMESTAMP | NOT NULL |

Ghi chu: Nen luu hash cua refresh token thay vi token goc.

## Content schema

### `languages`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| code | VARCHAR(10) | UNIQUE, NOT NULL |
| name | VARCHAR(100) | NOT NULL |
| active | BOOLEAN | NOT NULL DEFAULT TRUE |

### `topics`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| name | VARCHAR(100) | UNIQUE, NOT NULL |
| description | VARCHAR(255) | NULL |
| active | BOOLEAN | NOT NULL DEFAULT TRUE |

### `tags`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| name | VARCHAR(50) | UNIQUE, NOT NULL |

### `decks`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| title | VARCHAR(150) | NOT NULL |
| description | TEXT | NULL |
| source_language_id | BIGINT | FK languages.id |
| target_language_id | BIGINT | FK languages.id |
| topic_id | BIGINT | FK topics.id |
| visibility | VARCHAR(30) | NOT NULL |
| status | VARCHAR(30) | NOT NULL |
| created_by | BIGINT | FK users.id, NOT NULL |
| approved_by | BIGINT | FK users.id, NULL |
| approved_at | TIMESTAMP | NULL |
| rejection_reason | TEXT | NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| deleted_at | TIMESTAMP | NULL |

Index de xuat:

1. `idx_decks_created_by`
2. `idx_decks_visibility_status`
3. `idx_decks_topic_id`
4. `idx_decks_target_language_id`

### `deck_tags`

| Column | Type | Constraint |
|---|---|---|
| deck_id | BIGINT | PK, FK decks.id |
| tag_id | BIGINT | PK, FK tags.id |

### `flashcards`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| deck_id | BIGINT | FK decks.id, NOT NULL |
| front_text | TEXT | NOT NULL |
| back_text | TEXT | NOT NULL |
| pronunciation | VARCHAR(255) | NULL |
| example_sentence | TEXT | NULL |
| note | TEXT | NULL |
| difficulty_level | VARCHAR(30) | NULL |
| card_order | INT | NOT NULL DEFAULT 0 |
| active | BOOLEAN | NOT NULL DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |
| deleted_at | TIMESTAMP | NULL |

Index de xuat:

1. `idx_flashcards_deck_id`
2. `idx_flashcards_active`

### `flashcard_media`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| flashcard_id | BIGINT | FK flashcards.id |
| media_type | VARCHAR(30) | NOT NULL |
| media_url | VARCHAR(500) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

## Learning schema

### `study_sessions`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users.id, NOT NULL |
| deck_id | BIGINT | FK decks.id, NOT NULL |
| started_at | TIMESTAMP | NOT NULL |
| ended_at | TIMESTAMP | NULL |
| total_cards | INT | NOT NULL DEFAULT 0 |
| reviewed_cards | INT | NOT NULL DEFAULT 0 |
| status | VARCHAR(30) | NOT NULL |

### `review_items`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users.id, NOT NULL |
| flashcard_id | BIGINT | FK flashcards.id, NOT NULL |
| ease_factor | DECIMAL(4,2) | NOT NULL DEFAULT 2.50 |
| interval_days | INT | NOT NULL DEFAULT 0 |
| repetition_count | INT | NOT NULL DEFAULT 0 |
| mastery_level | VARCHAR(30) | NOT NULL DEFAULT 'NEW' |
| last_review_at | TIMESTAMP | NULL |
| next_review_at | TIMESTAMP | NULL |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

Constraint va index:

1. UNIQUE `user_id, flashcard_id`
2. `idx_review_items_user_next_review`
3. `idx_review_items_flashcard_id`

### `review_logs`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| review_item_id | BIGINT | FK review_items.id, NOT NULL |
| study_session_id | BIGINT | FK study_sessions.id, NULL |
| quality_score | INT | NOT NULL |
| rating | VARCHAR(30) | NOT NULL |
| response_time_ms | BIGINT | NULL |
| reviewed_at | TIMESTAMP | NOT NULL |

### `learning_progress`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | FK users.id, NOT NULL |
| deck_id | BIGINT | FK decks.id, NOT NULL |
| learned_cards | INT | NOT NULL DEFAULT 0 |
| mastered_cards | INT | NOT NULL DEFAULT 0 |
| completion_rate | DECIMAL(5,2) | NOT NULL DEFAULT 0 |
| last_studied_at | TIMESTAMP | NULL |
| updated_at | TIMESTAMP | NOT NULL |

Constraint: UNIQUE `user_id, deck_id`.

### `streaks`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| user_id | BIGINT | UNIQUE, FK users.id |
| current_streak_days | INT | NOT NULL DEFAULT 0 |
| best_streak_days | INT | NOT NULL DEFAULT 0 |
| last_study_date | DATE | NULL |
| updated_at | TIMESTAMP | NOT NULL |

## Admin schema

### `audit_logs`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| actor_id | BIGINT | FK users.id |
| action | VARCHAR(100) | NOT NULL |
| resource_type | VARCHAR(50) | NOT NULL |
| resource_id | BIGINT | NULL |
| details | TEXT | NULL |
| created_at | TIMESTAMP | NOT NULL |

### `reports`

| Column | Type | Constraint |
|---|---|---|
| id | BIGSERIAL | PK |
| reporter_id | BIGINT | FK users.id |
| target_type | VARCHAR(30) | NOT NULL |
| target_id | BIGINT | NOT NULL |
| reason | TEXT | NOT NULL |
| status | VARCHAR(30) | NOT NULL |
| created_at | TIMESTAMP | NOT NULL |
| resolved_at | TIMESTAMP | NULL |

## Seed data can co

### Roles

1. `ADMIN`
2. `CONTENT_MANAGER`
3. `LEARNER`

### Permissions MVP

1. `USER_READ`
2. `USER_MANAGE_STATUS`
3. `DECK_CREATE`
4. `DECK_READ`
5. `DECK_UPDATE_OWN`
6. `DECK_DELETE_OWN`
7. `DECK_APPROVE`
8. `FLASHCARD_CREATE`
9. `FLASHCARD_UPDATE_OWN`
10. `LEARNING_REVIEW`
11. `ADMIN_DASHBOARD_READ`

### Languages mau

1. `en` - English
2. `vi` - Vietnamese
3. `ja` - Japanese
4. `ko` - Korean
5. `zh` - Chinese

### Topics mau

1. Daily Life
2. Travel
3. Business
4. Grammar
5. Exam Preparation
