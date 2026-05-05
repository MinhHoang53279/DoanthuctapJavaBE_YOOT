# Database Design

## Nguyen tac thiet ke

1. MongoDB la database chinh cho MVP.
2. Project dung Spring Data MongoDB, `MongoRepository`, `MongoTemplate` cho query dong va `@Document` cho collection mapping.
3. Collection/index duoc tao tu MongoDB config va annotation.
4. `spring.data.mongodb.auto-index-creation=true` bat auto index tu annotation.
5. ID nghiep vu tiep tuc dung `Long` de giu API on dinh; collection `database_sequences` cap phat sequence.
6. Quan he chinh dung `@DBRef`; profile cua user duoc embed trong document `users`.
7. Cac collection lich su nhu `review_logs` va `audit_logs` la append-only o tang business.
8. Soft delete dung `deleted_at` cho deck/flashcard va user neu can giu lich su hoc.

## Collection plan

| Module | Collection |
|---|---|
| Identity | `users`, `roles`, `permissions` |
| Auth | `refresh_tokens` |
| Content | `languages`, `topics`, `tags`, `decks`, `flashcards` |
| Learning | `study_sessions`, `review_items`, `review_logs`, `learning_progress`, `streaks` |
| Admin | `audit_logs`, `reports` |
| Infra | `database_sequences` |

## Common fields

| Field | Type | Ghi chu |
|---|---|---|
| `_id` | Long/String | Long cho domain document, String cho `database_sequences` |
| `created_at` | LocalDateTime | Gan tu before-convert callback neu field ton tai |
| `updated_at` | LocalDateTime | Cap nhat tu before-convert callback neu field ton tai |
| `deleted_at` | LocalDateTime | Soft delete, nullable |
| enum | String | Luu enum name |
| DBRef | Reference | Luu tham chieu den document lien quan |

## Identity collections

### `users`

Core fields:

1. `_id`, `email`, `username`, `password_hash`, `status`
2. `profile` embedded object: `full_name`, `avatar_url`, `native_language_code`, `target_language_code`, `timezone`, `bio`
3. `roles` DBRef set den `roles`
4. `created_at`, `updated_at`, `deleted_at`

Indexes/rules:

1. Unique `email`
2. Unique `username`
3. Index `status`

### `roles`

Core fields: `_id`, `name`, `description`, `permissions`.

Indexes/rules:

1. Unique `name`
2. `permissions` DBRef set den `permissions`

### `permissions`

Core fields: `_id`, `code`, `description`.

Indexes/rules:

1. Unique `code`

## Auth collections

### `refresh_tokens`

Core fields:

1. `_id`, `user` DBRef, `token_hash`
2. `expires_at`, `revoked`, `revoked_at`, `created_at`

Indexes/rules:

1. Unique `token_hash`
2. Index `user`
3. Chi luu hash cua refresh token, khong luu token goc.

## Content collections

### `languages`

Core fields: `_id`, `code`, `name`, `active`.

Indexes/rules:

1. Unique `code`
2. Index `active`

### `topics`

Core fields: `_id`, `name`, `description`, `active`.

Indexes/rules:

1. Unique `name`
2. Index `active`

### `tags`

Core fields: `_id`, `name`.

Indexes/rules:

1. Unique `name`

### `decks`

Core fields:

1. `_id`, `title`, `description`
2. `source_language`, `target_language`, `topic`, `tags`
3. `visibility`: `PRIVATE`, `PUBLIC`
4. `status`: `DRAFT`, `PENDING`, `APPROVED`, `REJECTED`
5. `created_by`, `approved_by`, `approved_at`, `rejection_reason`
6. `created_at`, `updated_at`, `deleted_at`

Indexes/rules:

1. Index `created_by`
2. Index `visibility` + `status`
3. Index `topic`
4. Index `target_language`
5. Public catalog chi tra deck `PUBLIC` + `APPROVED` + chua soft delete.

### `flashcards`

Core fields:

1. `_id`, `deck`, `front_text`, `back_text`
2. `pronunciation`, `example_sentence`, `note`, `difficulty_level`, `card_order`, `active`
3. `created_at`, `updated_at`, `deleted_at`

Indexes/rules:

1. Index `deck`
2. Index `active`
3. Query hoc tap chi dung flashcard active va chua soft delete.

## Learning collections

### `study_sessions`

Core fields:

1. `_id`, `user`, `deck`
2. `started_at`, `ended_at`, `total_cards`, `reviewed_cards`, `status`

Indexes/rules:

1. Index `user`
2. Index `deck`

### `review_items`

Core fields:

1. `_id`, `user`, `flashcard`
2. `ease_factor`, `interval_days`, `repetition_count`, `mastery_level`
3. `last_review_at`, `next_review_at`, `created_at`, `updated_at`

Indexes/rules:

1. Unique logical rule: one review item per `(user, flashcard)`.
2. Index `user` + `next_review_at` cho due cards.
3. Index `flashcard`.

### `review_logs`

Core fields:

1. `_id`, `review_item`, `study_session`
2. `quality_score`, `rating`, `response_time_ms`, `reviewed_at`

Rules:

1. Append-only. Khong update/xoa log review trong business flow thong thuong.

### `learning_progress`

Core fields:

1. `_id`, `user`, `deck`
2. `learned_cards`, `mastered_cards`, `completion_rate`, `last_studied_at`, `updated_at`

Indexes/rules:

1. Unique logical rule: one progress per `(user, deck)`.
2. Index `user`.

### `streaks`

Core fields: `_id`, `user`, `current_streak_days`, `best_streak_days`, `last_study_date`, `updated_at`.

Indexes/rules:

1. Unique logical rule: one streak document per user.

## Admin collections

### `audit_logs`

Core fields:

1. `_id`, `actor`, `action`, `resource_type`, `resource_id`, `details`, `created_at`

Rules:

1. Append-only cho thao tac quan tri quan trong.

### `reports`

Core fields:

1. `_id`, `reporter`, `target_type`, `target_id`, `reason`, `status`, `created_at`, `resolved_at`

Indexes/rules:

1. Index `status`
2. Index `target_type` + `target_id`

## Seed data

Seed data duoc tao boi `MongoSeedDataInitializer` khi app start.

### Roles

1. `LEARNER`
2. `CONTENT_MANAGER`
3. `ADMIN`
4. `SUPER_ADMIN`

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
12. `REPORT_READ`
13. `REPORT_MANAGE`
14. `AUDIT_LOG_READ`

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
