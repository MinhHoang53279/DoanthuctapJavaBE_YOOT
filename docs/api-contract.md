# API Contract

## Quy uoc chung

Base URL local:

```text
http://localhost:8080/api/v1
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

## Response format

Tat ca API nen tra ve format thong nhat:

```json
{
  "success": true,
  "message": "Success",
  "data": {},
  "timestamp": "2026-04-24T10:00:00Z"
}
```

Validation error:

```json
{
  "success": false,
  "message": "Validation failed",
  "data": {
    "email": "Email must be valid"
  },
  "timestamp": "2026-04-24T10:00:00Z"
}
```

## Pagination format

```json
{
  "items": [],
  "page": 0,
  "size": 20,
  "totalItems": 100,
  "totalPages": 5
}
```

## Status code

| Code | Khi nao dung |
|---|---|
| 200 | Request thanh cong |
| 201 | Tao moi thanh cong |
| 204 | Xoa hoac logout thanh cong, khong can body |
| 400 | Validation fail hoac request sai |
| 401 | Chua dang nhap hoac token sai |
| 403 | Khong du quyen |
| 404 | Khong tim thay resource |
| 409 | Trung du lieu hoac conflict nghiep vu |
| 500 | Loi he thong khong mong muon |

## Auth APIs

### Register

`POST /auth/register`

Public endpoint.

Request:

```json
{
  "email": "learner@example.com",
  "username": "learner01",
  "password": "Password@123",
  "fullName": "Learner One"
}
```

Response `201`:

```json
{
  "id": 1,
  "email": "learner@example.com",
  "username": "learner01",
  "status": "ACTIVE"
}
```

Validation:

1. Email hop le va duy nhat.
2. Username 3 den 100 ky tu va duy nhat.
3. Password toi thieu 8 ky tu.

### Login

`POST /auth/login`

Public endpoint.

Request:

```json
{
  "usernameOrEmail": "learner@example.com",
  "password": "Password@123"
}
```

Response `200`:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Refresh token

`POST /auth/refresh`

Public endpoint, nhung can refresh token hop le.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response `200`:

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Logout

`POST /auth/logout`

Authenticated endpoint.

Request:

```json
{
  "refreshToken": "refresh-token"
}
```

Response `204`.

### Current user

`GET /auth/me`

Authenticated endpoint.

Response `200`:

```json
{
  "id": 1,
  "email": "learner@example.com",
  "username": "learner01",
  "fullName": "Learner One",
  "roles": ["LEARNER"]
}
```

## User APIs

### List users

`GET /users?page=0&size=20&keyword=&status=`

Required role: `ADMIN`.

### Get user detail

`GET /users/{id}`

Required role: `ADMIN` or current owner.

### Update my profile

`PUT /users/me/profile`

Authenticated endpoint.

Request:

```json
{
  "fullName": "Learner One",
  "avatarUrl": "https://example.com/avatar.png",
  "nativeLanguageCode": "vi",
  "targetLanguageCode": "en",
  "timezone": "Asia/Ho_Chi_Minh",
  "bio": "I am learning English"
}
```

### Update user status

`PATCH /users/{id}/status`

Required role: `ADMIN`.

Request:

```json
{
  "status": "LOCKED",
  "reason": "Violation of content policy"
}
```

## Content APIs

### List languages

`GET /languages`

Public or authenticated endpoint.

### Create language

`POST /languages`

Required role: `ADMIN`.

Request:

```json
{
  "code": "en",
  "name": "English"
}
```

### List topics

`GET /topics`

Public or authenticated endpoint.

### Create topic

`POST /topics`

Required role: `ADMIN` or `CONTENT_MANAGER`.

Request:

```json
{
  "name": "Daily Life",
  "description": "Common vocabulary for daily life"
}
```

## Deck APIs

### Create deck

`POST /decks`

Authenticated endpoint.

Request:

```json
{
  "title": "English A1 Daily Vocabulary",
  "description": "Basic words for beginners",
  "sourceLanguageId": 2,
  "targetLanguageId": 1,
  "topicId": 1,
  "visibility": "PRIVATE",
  "tagIds": [1, 2]
}
```

### List decks

`GET /decks?page=0&size=20&keyword=&topicId=&languageId=&visibility=`

Rule:

1. Public catalog chi hien deck `PUBLIC + APPROVED`.
2. User authenticated co the thay deck cua minh.
3. Admin co the thay tat ca.

### Get deck detail

`GET /decks/{id}`

### Update deck

`PUT /decks/{id}`

Required: owner hoac role co quyen quan ly content.

### Delete deck

`DELETE /decks/{id}`

Nen soft delete neu deck da co review history.

### Submit deck for review

`POST /decks/{id}/submit-review`

Owner hoac Content Manager.

### Approve deck

`PATCH /admin/decks/{id}/approve`

Required role: `ADMIN` hoac `CONTENT_MANAGER` tuy chinh sach.

### Reject deck

`PATCH /admin/decks/{id}/reject`

Request:

```json
{
  "reason": "Deck contains invalid translations"
}
```

## Flashcard APIs

### Create flashcard

`POST /decks/{deckId}/flashcards`

Request:

```json
{
  "frontText": "apple",
  "backText": "qua tao",
  "pronunciation": "/ˈæp.əl/",
  "exampleSentence": "I eat an apple every day.",
  "note": "Noun",
  "difficultyLevel": "EASY",
  "cardOrder": 1
}
```

### List flashcards in deck

`GET /decks/{deckId}/flashcards?page=0&size=50`

### Update flashcard

`PUT /flashcards/{id}`

### Delete flashcard

`DELETE /flashcards/{id}`

## Learning APIs

### Start study session

`POST /study-sessions/start`

Request:

```json
{
  "deckId": 1,
  "limit": 20
}
```

Response:

```json
{
  "sessionId": 100,
  "deckId": 1,
  "cards": [
    {
      "flashcardId": 10,
      "frontText": "apple",
      "backText": "qua tao",
      "masteryLevel": "NEW"
    }
  ]
}
```

### Get reviews today

`GET /reviews/today?limit=50`

Authenticated endpoint.

### Submit review

`POST /reviews/{flashcardId}`

Request:

```json
{
  "studySessionId": 100,
  "rating": "GOOD",
  "responseTimeMs": 3200
}
```

Response:

```json
{
  "flashcardId": 10,
  "masteryLevel": "LEARNING",
  "intervalDays": 1,
  "nextReviewAt": "2026-04-25T10:00:00Z"
}
```

### Finish study session

`POST /study-sessions/{id}/finish`

Authenticated endpoint.

### Get my progress

`GET /progress/me`

Authenticated endpoint.

### Get progress by deck

`GET /progress/decks/{deckId}`

Authenticated endpoint.

## Admin APIs

### Dashboard

`GET /admin/dashboard`

Required role: `ADMIN`.

Response:

```json
{
  "totalUsers": 100,
  "totalDecks": 30,
  "totalFlashcards": 900,
  "totalStudySessions": 1200,
  "pendingDecks": 5
}
```

### Reports

`GET /admin/reports?page=0&size=20&status=PENDING`

Required role: `ADMIN`.

### Resolve report

`PATCH /admin/reports/{id}/resolve`

Required role: `ADMIN`.

Request:

```json
{
  "status": "RESOLVED",
  "note": "Deck was archived"
}
```

## API versioning

1. Version dau tien la `/api/v1`.
2. Khong doi response shape trong cung version neu khong can thiet.
3. Neu co breaking change lon, tao `/api/v2`.
