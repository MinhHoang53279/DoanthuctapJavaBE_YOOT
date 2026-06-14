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
├── bo test api/             # Postman automation collections
├── .env.example             # Bien moi truong mau
└── README.md
```

## Yeu Cau Moi Truong

- JDK 21+
- Maven 3.9+
- MongoDB local tai `localhost:27017`
- Git
- Postman neu muon chay automation collection

Kiem tra nhanh:

```bash
java -version
mvn -version
git --version
```

## Cai Dat Nhanh

### 1. Clone source

```bash
git clone https://github.com/MinhHoang53279/DoanthuctapJavaBE_YOOT.git
cd DoanthuctapJavaBE_YOOT
```

Neu dang co source san tren may thi mo terminal tai thu muc root cua project:

```text
E:\DoAnThucTapJavaBE_YOOT
```

### 2. Kiem tra cau truc project

```bash
dir
```

Can thay cac thu muc/file chinh:

```text
backend/
bo test api/
.env.example
README.md
```

### 3. Cau hinh bien moi truong local

Project co the chay ngay voi profile `dev` ma khong bat buoc tao `.env`, vi `application.yml` da co default local.

Neu muon custom MongoDB/JWT/port, tao file `.env` tu mau:

```powershell
Copy-Item .env.example .env
```

Luu y: khong commit `.env` that. Khi chay `mvn spring-boot:run`, Spring Boot doc bien moi truong tu OS environment; neu can override trong PowerShell, set bien bang `$env:...`.

Vi du set bien moi truong khi chay local:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:MONGODB_URI="mongodb://localhost:27017/flashcard_platform"
$env:JWT_SECRET="dev-only-jwt-secret-please-change-in-production-32bytes"
```

### 4. Chay MongoDB local

Dam bao MongoDB local dang chay tai `localhost:27017`.

MongoDB mac dinh:

```text
mongodb://localhost:27017/flashcard_platform
```

### 5. Build backend

```bash
cd backend
mvn -q -DskipTests compile
cd ..
```

### 6. Chay backend

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

### 7. Chay test

```bash
cd backend
mvn test
```

Integration test dung MongoDB local. Neu muon tach database test:

```powershell
$env:MONGODB_TEST_URI="mongodb://localhost:27017/flashcard_platform_test"
mvn test
```

## Bien Moi Truong

Co the tao `.env` tu `.env.example` neu can override cau hinh local. Khong commit `.env` that.

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
| `SERVER_PORT` | `8080` | Port backend |

Vi du `.env` local:

```env
MONGODB_URI=mongodb://localhost:27017/flashcard_platform
JWT_SECRET=replace-with-at-least-32-random-characters
ACCESS_TOKEN_EXPIRATION_MS=3600000
REFRESH_TOKEN_EXPIRATION_MS=604800000
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
SERVER_PORT=8080
```

Chay backend voi bien moi truong tren PowerShell:

```powershell
cd backend
$env:MONGODB_URI="mongodb://localhost:27017/flashcard_platform"
$env:JWT_SECRET="dev-only-jwt-secret-please-change-in-production-32bytes"
mvn spring-boot:run
```

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

Trong production, bat buoc set `JWT_SECRET` that va khong dung secret dev.

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

Chi tiet API xem trong Swagger UI.

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

Thu muc `bo test api/` co 10 Postman collection:

- Learner auth lifecycle
- Learner private deck and learning
- Content manager curation
- Admin moderation/governance
- Security and boundary regression
- Public catalog explorer
- Refresh token abuse checks
- Ownership isolation
- Review rating matrix
- Admin audit and filter checks

Import cac file `*.postman_collection.json` vao Postman. Co the import them:

```text
bo test api/flashcard_local.postman_environment.json
```

Moi collection dong vai mot nguoi dung that khac nhau va bao phu cac luong Auth, Content, Learning, Admin, Security, public catalog, ownership va audit.

## Ghi Chu Bao Mat

- Khong commit `.env` that.
- Khong dung JWT secret dev cho production.
- Refresh token duoc luu dang hash, khong luu token goc.
- API private yeu cau Bearer JWT.

## Tac Gia

Pham Dong Minh Hoang

Email: phamdongminhhoang@gmail.com
