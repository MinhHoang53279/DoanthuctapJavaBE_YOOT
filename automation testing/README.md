# Flashcard Postman Automation Testing

Thu muc nay gom 5 Postman collection import-chay truc tiep cho backend Flashcard Learning Platform.

## Files

1. `01_minh_learner_auth_lifecycle.postman_collection.json`
   - Persona: Minh, learner moi.
   - Scope: register, duplicate register, login, me, refresh rotation, logout.
   - 28 `pm.test` test cases.

2. `02_han_learner_private_deck_learning.postman_collection.json`
   - Persona: Han, learner hoc hang ngay.
   - Scope: private deck, flashcards, study session, review, progress, cleanup.
   - 27 `pm.test` test cases.

3. `03_linh_content_manager_curation.postman_collection.json`
   - Persona: Linh, content manager.
   - Scope: tag/topic, public deck curation, approve/reject, catalog visibility.
   - 27 `pm.test` test cases.
   - Can tai khoan co role `CONTENT_MANAGER`, `ADMIN`, hoac `SUPER_ADMIN`.

4. `04_anh_admin_moderation_governance.postman_collection.json`
   - Persona: Anh, admin.
   - Scope: dashboard, user lock/unlock, deck approval, report, audit logs.
   - 25 `pm.test` test cases.
   - Can tai khoan co role `ADMIN` hoac `SUPER_ADMIN`.

5. `05_khoa_security_boundary_regression.postman_collection.json`
   - Persona: Khoa, QA/security tester.
   - Scope: 401, 403, validation 400, not found 404, invalid token.
   - 27 `pm.test` test cases.

Optional:

- `flashcard_local.postman_environment.json`: environment mau.
- `generate-postman-collections.js`: source generator de sua/regenerate collection.

## Cach import vao Postman

1. Mo Postman.
2. Chon `Import`.
3. Import 5 file `*.postman_collection.json`.
4. Neu muon, import them `flashcard_local.postman_environment.json`.
5. Chon environment `Flashcard Local MongoDB`.
6. Dam bao backend dang chay:

```bash
cd backend
mvn spring-boot:run
```

Base URL mac dinh:

```text
http://localhost:8080/api/v1
```

## Cach chay dung

- Chay tung collection bang `Run collection`.
- Luon chay tu request dau tien `00 - Prepare fresh persona run`.
- Request prepare se tao `runId`, email/username rieng, tranh trung data moi lan run.
- Collection 01, 02, 05 tu tao learner rieng.
- Collection 03, 04 can credential co role cao hon learner.

## Tao tai khoan admin/content manager cho collection 03 va 04

API register hien tai chi tao role `LEARNER`, nen ban can tao/promote user trong MongoDB local.

1. Register user bang API hoac Postman:

```json
{
  "email": "admin@example.com",
  "username": "admin_local",
  "password": "Password@123",
  "fullName": "Admin Local"
}
```

2. Promote trong mongosh:

```javascript
use flashcard_platform
const adminRole = db.roles.findOne({ name: "ADMIN" })
db.users.updateOne(
  { email: "admin@example.com" },
  { $set: { roles: [DBRef("roles", adminRole._id)] } }
)
```

Content manager:

```javascript
use flashcard_platform
const managerRole = db.roles.findOne({ name: "CONTENT_MANAGER" })
db.users.updateOne(
  { email: "manager@example.com" },
  { $set: { roles: [DBRef("roles", managerRole._id)] } }
)
```

3. Trong Postman, sua variables:

- `managerEmail`
- `managerPassword`
- `adminEmail`
- `adminPassword`

Co the sua trong tab `Variables` cua collection hoac trong environment.

## Cach sua script

Cach nhanh nhat:

1. Sua file `generate-postman-collections.js`.
2. Chay:

```bash
node "automation testing/generate-postman-collections.js"
```

3. Import lai collection JSON vao Postman.

Neu sua truc tiep trong Postman:

1. Mo collection.
2. Chon request can sua.
3. Sua tab `Body`, `Authorization`, `Pre-request Script`, hoac `Tests`.
4. Export lai collection neu muon luu ra file.

## Cac bien quan trong

- `baseUrl`: doi port/domain backend.
- `runId`: auto set moi lan chay collection tu request dau.
- `password`: password mac dinh cho learner auto-created.
- `managerEmail`, `managerPassword`: credential content manager.
- `adminEmail`, `adminPassword`: credential admin.

## Luu y

- Cac collection tao du lieu test tren MongoDB local.
- Collection 02 co cleanup deck/flashcard cuoi flow.
- Collection 03/04 co tao deck/report/audit log de test workflow quan tri.
- Khong can push code hay chay migration SQL.
