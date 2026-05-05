# Architecture Blueprint

## Kien truc tong the

Backend duoc thiet ke theo mo hinh `modular monolith` tren Spring Boot. Cac module tach theo domain nghiep vu, nhung van deploy thanh mot service duy nhat de don gian hoa viec phat trien, test va van hanh.

## Module nghiep vu

1. `auth`
   - dang ky, dang nhap, refresh token, current user
2. `identity`
   - users, roles, permissions, profiles
3. `content`
   - languages, topics, tags, decks, flashcards, media
4. `learning`
   - study sessions, review items, review logs, progress, streaks
5. `assessment`
   - quizzes, questions, attempts, results
6. `classroom`
   - classes, members, deck assignments
7. `admin`
   - dashboard, moderation, reports, audit logs, notifications

## Package structure de xuat

```text
com.yoot.flashcard
├── common
│   ├── config
│   ├── exception
│   ├── response
│   ├── security
│   └── audit
├── modules
│   ├── auth
│   │   ├── controller
│   │   ├── dto
│   │   ├── service
│   │   ├── repository
│   │   └── mapper
│   ├── identity
│   │   ├── controller
│   │   ├── dto
│   │   ├── service
│   │   ├── repository
│   │   └── entity
│   ├── content
│   ├── learning
│   ├── assessment
│   ├── classroom
│   └── admin
└── FlashcardApplication
```

## Quy uoc ben trong moi module

Moi module nen theo cau truc:

1. `controller`
   - REST endpoints
2. `dto`
   - request/response object
3. `entity`
   - MongoDB document/entity
4. `repository`
   - Spring Data repository
5. `service`
   - business logic
6. `mapper`
   - chuyen doi entity <-> dto

## Luong xu ly chuan

```text
Controller -> Service -> Repository -> Database
          -> Mapper  -> DTO
```

Cross-cutting concerns di qua `common`:

1. `security`
2. `exception`
3. `response`
4. `config`

## Nguyen tac ky thuat

1. DTO khong dung chung voi Entity.
2. Validation dat o request DTO.
3. Service chua nghiep vu, Controller khong viet logic lon.
4. Mapper chuyen doi du lieu, tranh lap code.
5. Global exception handler tra response thong nhat.
6. Security dua tren role/permission.
7. MongoDB collections, indexes va seed data duoc quan ly bang Spring Data MongoDB config/initializer.

## Roadmap code theo giai doan

### Phase 1 - Foundation

1. Khoi tao Spring Boot project
2. Config MongoDB
3. Config response wrapper va exception handler
4. Config Spring Security + JWT
5. Hoan thanh `auth` + `identity`

### Phase 2 - Content

1. Tao entity cho `languages`, `topics`, `tags`
2. Tao entity cho `decks`, `flashcards`
3. CRUD APIs cho hoc lieu
4. Seed data mau

### Phase 3 - Learning Engine

1. Tao `study_sessions`, `review_items`, `review_logs`
2. Viet review scheduling service
3. API hoc hom nay / nop ket qua on tap
4. Tinh `learning_progress`, `streaks`

### Phase 4 - Assessment & Classroom

1. Tao `quizzes`, `quiz_questions`, `quiz_attempts`
2. Tao `classes`, `class_members`, `deck_assignments`
3. Instructor APIs va learner tracking

### Phase 5 - Administration

1. Moderation deck/public content
2. Dashboard va report APIs
3. Audit log
4. Notification co ban

## Thu tu code entity hop ly

1. `User`, `Role`, `Permission`
2. `Language`, `Topic`, `Tag`
3. `Deck`, `Flashcard`
4. `StudySession`, `ReviewItem`, `ReviewLog`
5. `LearningProgress`, `Streak`
6. `Quiz`, `QuizQuestion`, `QuizAttempt`
7. `Classroom`, `ClassMember`, `DeckAssignment`
8. `Report`, `Notification`, `AuditLog`

## API group de mo ta Swagger

1. `/api/v1/auth/**`
2. `/api/v1/users/**`
3. `/api/v1/content/**`
4. `/api/v1/learning/**`
5. `/api/v1/quizzes/**`
6. `/api/v1/classes/**`
7. `/api/v1/admin/**`
