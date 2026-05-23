# Flashcard Postman Automation Testing

Thu muc nay gom 10 Postman collection import-chay truc tiep cho backend Flashcard Learning Platform.

## Files

1. `01_minh_learner_auth_lifecycle.postman_collection.json`
   - Persona: Minh, learner moi.
   - Scope: register, duplicate register, login, me, refresh rotation, logout.
   - 27 `pm.test` test cases.

2. `02_han_learner_private_deck_learning.postman_collection.json`
   - Persona: Han, learner hoc hang ngay.
   - Scope: private deck, flashcards, study session, review, progress, cleanup.
   - 27 `pm.test` test cases.

6. `06_vy_public_catalog_explorer.postman_collection.json`
   - Persona: Vy, visitor chua dang nhap.
   - Scope: health check, public catalog, search/filter/pagination, protected boundary.
   - 23 `pm.test` test cases.

7. `07_quan_refresh_token_abuse_checks.postman_collection.json`
   - Persona: Quan, learner kiem tra refresh token.
   - Scope: refresh token thieu/sai, refresh sau login, token cu, logout revoke, relogin.
   - 23 `pm.test` test cases.

8. `08_mai_nam_ownership_isolation.postman_collection.json`
   - Persona: Mai va Nam, hai learner doc lap.
   - Scope: private deck ownership, cross-user isolation, public approved visibility boundary.
   - 21 `pm.test` test cases.

9. `09_oanh_review_rating_matrix.postman_collection.json`
   - Persona: Oanh, learner luyen tap rating.
   - Scope: 4 flashcard, review ratings `AGAIN/HARD/GOOD/EASY`, review log, due card, progress.
   - 25 `pm.test` test cases.

10. `10_bao_admin_audit_filter_checks.postman_collection.json`
   - Persona: Bao, admin kiem tra governance.
   - Scope: dashboard, filter audit, lock/unlock user, reject/approve deck, report boundary.
   - 22 `pm.test` test cases.
   - Can tai khoan co role `ADMIN` hoac `SUPER_ADMIN`.

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
- `generate-postman-collections.js`: source generator de sua/regenerate collection 01-05.
- `generate-extra-postman-collections.js`: source generator de sua/regenerate collection 06-10.

## Cach import vao Postman

1. Mo Postman.
2. Chon `Import`.
3. Import 10 file `*.postman_collection.json`.
4. Neu muon, import them `flashcard_local.postman_environment.json`.
5. Chon environment `Flashcard Local MongoDB`.
6. Dam bao backend dang chay:

```bash
cd backend
mvn spring-boot:run
```

Profile mac dinh la `dev` voi JWT secret co san, khong can set bien moi truong them. Neu chay tren profile khac, nho set `JWT_SECRET`.

Base URL mac dinh:

```text
http://localhost:8080/api/v1
```

## Cach chay dung

- Chay tung collection bang `Run collection`.
- Luon chay tu request dau tien `00 - Prepare fresh persona run`.
- Request prepare se tao `runId`, email/username rieng, tranh trung data moi lan run.
- Collection 01, 02, 05, 07, 08, 09 tu tao learner rieng.
- Collection 06 chay nhu visitor/public user.
- Collection 03, 04, 10 can credential co role cao hon learner.

## Tao tai khoan admin/content manager cho collection 03, 04 va 10

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

3. Tai khoan admin local da tao co the dung truc tiep cho collection 03, 04 va 10:

```text
adminEmail: admin@example.com
adminPassword: Password@123
managerEmail: admin@example.com
managerPassword: Password@123
```

4. Trong Postman, sua variables neu ban muon dung tai khoan khac:

- `managerEmail`
- `managerPassword`
- `adminEmail`
- `adminPassword`

Co the sua trong tab `Variables` cua collection hoac trong environment.

## Cach sua script

Cach nhanh nhat:

1. Sua file `generate-postman-collections.js` neu can sua collection 01-05.
2. Sua file `generate-extra-postman-collections.js` neu can sua collection 06-10.
3. Chay generator tuong ung:

```bash
node "automation testing/generate-postman-collections.js"
node "automation testing/generate-extra-postman-collections.js"
```

4. Import lai collection JSON vao Postman.

Neu sua truc tiep trong Postman:

1. Mo collection.
2. Chon request can sua.
3. Sua tab `Body`, `Authorization`, `Pre-request Script`, hoac `Tests`.
4. Export lai collection neu muon luu ra file.

## Cac bien quan trong

- `baseUrl`: doi port/domain backend.
- `runId`: auto set moi lan chay collection tu request dau.
- `password`: password mac dinh cho learner auto-created.
- `managerEmail`, `managerPassword`: credential content manager; mac dinh dang dung admin local vi role `ADMIN` du quyen chay flow nay.
- `adminEmail`, `adminPassword`: credential admin.

## Luu y

- Cac collection tao du lieu test tren MongoDB local.
- Collection 02 co cleanup deck/flashcard cuoi flow.
- Collection 03/04/10 co tao deck/report/audit log de test workflow quan tri.
- Khong can push code hay chay migration SQL.
