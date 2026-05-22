# Flashcard Learning Platform Backend

Backend API cho nen tang hoc flashcard, duoc xay theo huong Spring Boot modular monolith. Project tap trung vao MVP gom Auth, Identity/RBAC, Content, Learning, Admin, Swagger va Postman automation.

## Tech Stack

- Java 21
- Spring Boot 3.3.5
- Spring Security, JWT, BCrypt
- Spring Data MongoDB
- MongoDB 7
- Maven
- Swagger/OpenAPI
- JUnit 5, Spring Boot Test
- Docker Compose cho MongoDB local

## Module Chinh

- `auth`: dang ky, dang nhap, refresh token, logout, current user.
- `identity`: user, profile embedded, role, permission, user status.
- `content`: language, topic, tag, deck, flashcard, public/private visibility.
- `learning`: study session, review item, review log, progress, streak, review scheduling.
- `admin`: dashboard, approve/reject deck, lock/unlock user, report, audit log.
- `common`: response wrapper, exception handler, security, config, MongoDB sequence/seed.

## Cau Truc Thu Muc

```text
.
├── backend/                 # Spring Boot backend
├── automation testing/      # Postman automation collections
├── docs/                    # Tai lieu PRD, architecture, database design, API contract
├── docker-compose.yml       # MongoDB local
├── .env.example             # Bien moi truong mau
└── README.md
```

## Yeu Cau Moi Truong

- JDK 21+
- Maven 3.9+
- Docker Desktop hoac MongoDB local tai `localhost:27017`

Kiem tra nhanh:

```bash
java -version
mvn -version
docker --version
```

## Chay MongoDB Local

Tu thu muc root:

```bash
docker compose up -d mongodb
```

MongoDB mac dinh:

```text
mongodb://localhost:27017/flashcard_platform
```

## Chay Backend

Profile mac dinh la `dev`. Profile `dev` co JWT secret chi de chay local nhanh, khong dung cho production.

```bash
cd backend
mvn spring-boot:run
```

Backend mac dinh chay tai:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health check:

```text
http://localhost:8080/actuator/health
```

## Bien Moi Truong

Co the tao `.env` tu `.env.example` neu can override cau hinh Docker/local. Khong commit `.env` that.

Bien quan trong:

| Bien | Mac dinh | Ghi chu |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Profile chay app |
| `MONGODB_URI` | `mongodb://localhost:27017/flashcard_platform` | MongoDB app database |
| `MONGODB_TEST_URI` | local test DB | MongoDB test database |
| `JWT_SECRET` | co default trong `dev` | Bat buoc set secret that khi chay `prod` |
| `ACCESS_TOKEN_EXPIRATION_MS` | `3600000` | 1 gio |
| `REFRESH_TOKEN_EXPIRATION_MS` | `604800000` | 7 ngay |
| `CORS_ALLOWED_ORIGINS` | localhost frontend ports | CORS dev |

Chay profile production:

```bash
cd backend
SPRING_PROFILES_ACTIVE=prod JWT_SECRET=<at-least-32-chars> mvn spring-boot:run
```

Tren PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="prod"
$env:JWT_SECRET="replace-with-at-least-32-random-characters"
mvn spring-boot:run
```

## Test

Compile:

```bash
cd backend
mvn -q -DskipTests compile
```

Chay test:

```bash
cd backend
mvn test
```

Integration test dung MongoDB local. Neu can test database rieng:

```bash
MONGODB_TEST_URI=mongodb://localhost:27017/flashcard_platform_test mvn test
```

## API MVP

Base API:

```text
http://localhost:8080/api/v1
```

Nhom endpoint chinh:

- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`
- `GET /auth/me`
- `GET /languages`
- `GET /topics`
- `GET /tags`
- `GET /decks`
- `POST /decks`
- `POST /decks/{id}/submit-review`
- `POST /decks/{deckId}/flashcards`
- `POST /study-sessions/start`
- `POST /reviews/{flashcardId}`
- `GET /progress/me`
- `GET /admin/dashboard`
- `POST /admin/decks/{id}/approve`
- `POST /admin/decks/{id}/reject`
- `POST /admin/users/{id}/lock`
- `POST /admin/users/{id}/unlock`
- `GET /admin/audit-logs`

Chi tiet API nam trong Swagger va `docs/api-contract.md`.

## Seed Data

Khi app start, `MongoSeedDataInitializer` seed du lieu nen:

- Roles: `LEARNER`, `CONTENT_MANAGER`, `ADMIN`, `SUPER_ADMIN`
- Permissions MVP
- Languages: English, Vietnamese, Japanese, Korean, Chinese
- Topics: Daily Life, Travel, Business, Grammar, Exam Preparation

Register API mac dinh tao user role `LEARNER`.

## Tao Admin/Content Manager Local

API register hien tai chi tao learner. De test admin/content manager, co the register user truoc, sau do promote trong `mongosh`.

Vi du promote admin:

```javascript
use flashcard_platform
const adminRole = db.roles.findOne({ name: "ADMIN" })
db.users.updateOne(
  { email: "admin@example.com" },
  { $set: { roles: [DBRef("roles", adminRole._id)] } }
)
```

Promote content manager:

```javascript
use flashcard_platform
const managerRole = db.roles.findOne({ name: "CONTENT_MANAGER" })
db.users.updateOne(
  { email: "manager@example.com" },
  { $set: { roles: [DBRef("roles", managerRole._id)] } }
)
```

## Postman Automation

Thu muc `automation testing/` co 5 Postman collection:

- Learner auth lifecycle
- Learner private deck and learning
- Content manager curation
- Admin moderation/governance
- Security and boundary regression

Import cac file `*.postman_collection.json` vao Postman. Co the import them:

```text
automation testing/flashcard_local.postman_environment.json
```

Huong dan chi tiet nam trong:

```text
automation testing/README.md
```

## Tai Lieu

- `docs/product-requirements.md`
- `docs/database-design.md`
- `docs/architecture.md`
- `docs/api-contract.md`
- `docs/security-design.md`
- `docs/learning-engine.md`
- `docs/development-roadmap.md`

## Ghi Chu Bao Mat

- Khong commit `.env` that.
- Khong dung JWT secret dev cho production.
- Refresh token duoc luu dang hash, khong luu token goc.
- API private yeu cau Bearer JWT.

## Tac Gia

Pham Dong Minh Hoang

Email: phamdongminhhoang@gmail.com
