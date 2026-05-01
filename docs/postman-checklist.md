# Postman MVP Checklist

Use the `test` or local dev database only. Do not run this checklist against production data.

## Setup

1. Set `baseUrl` to `http://localhost:8080`.
2. Keep collection variables: `accessToken`, `refreshToken`, `deckId`, `flashcardId`, `studySessionId`, `reportId`.
3. Send authenticated requests with `Authorization: Bearer {{accessToken}}`.

## Auth

1. `POST {{baseUrl}}/api/v1/auth/register` creates a learner.
2. `POST {{baseUrl}}/api/v1/auth/login` returns access and refresh tokens.
3. `GET {{baseUrl}}/api/v1/auth/me` returns 401 without token and 200 with token.
4. `POST {{baseUrl}}/api/v1/auth/refresh` rotates refresh token.
5. Reusing an old refresh token returns 401.
6. `POST {{baseUrl}}/api/v1/auth/logout` revokes refresh token.

## Content

1. `GET /api/v1/languages`, `/topics`, `/tags` return seeded catalog data.
2. Learner can create a `PRIVATE` deck with `POST /api/v1/decks`.
3. Anonymous user cannot read a private deck.
4. Owner can create, update, list, and delete flashcards.
5. Learner cannot create a `PUBLIC` deck.
6. Content manager can create a `PUBLIC` deck and submit it with `POST /api/v1/decks/{id}/submit-review`.
7. Public catalog shows only decks with `visibility=PUBLIC` and `status=APPROVED`.

## Learning

1. `POST /api/v1/study-sessions/start` returns due/new cards.
2. `POST /api/v1/reviews/{flashcardId}` accepts `AGAIN`, `HARD`, `GOOD`, `EASY`.
3. Review submission creates append-only review log data.
4. `GET /api/v1/progress/decks/{deckId}` updates learned cards and completion rate.
5. `GET /api/v1/progress/me` updates current and best streak.
6. `POST /api/v1/study-sessions/{id}/finish` closes the session.

## Admin

1. Learner receives 403 for `GET /api/v1/admin/dashboard`.
2. Admin receives counts for users, decks, flashcards, sessions, and open reports.
3. `POST /api/v1/admin/decks/{id}/approve` approves a pending public deck.
4. `POST /api/v1/admin/decks/{id}/reject` rejects a pending public deck with a reason.
5. `POST /api/v1/reports` creates a content report.
6. `GET /api/v1/admin/reports` lists reports with status/target filters.
7. `PATCH /api/v1/admin/reports/{id}/status` resolves or dismisses a report.
8. `POST /api/v1/admin/users/{id}/lock` blocks future login and private API access.
9. `POST /api/v1/admin/users/{id}/unlock` restores login.
10. `GET /api/v1/admin/audit-logs` shows admin operations for deck moderation, user status changes, and report handling.

## Security And Docs

1. Invalid bearer token returns 401.
2. Valid token without required permission returns 403.
3. Swagger UI is available at `/swagger-ui/index.html`.
4. OpenAPI docs expose Bearer JWT auth and groups for auth, identity, content, learning, and admin.
