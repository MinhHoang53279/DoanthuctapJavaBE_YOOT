# Development Roadmap

## Nguyen tac trien khai

1. Khong code khi chua chot thiet ke.
2. Moi phase phai co document/collection design, entity, repository, service, controller va test neu la code phase.
3. Uu tien nho ma dung, khong nhay sang microservices.
4. Swagger phai cap nhat theo tung API.
5. Khong hardcode secret production.
6. Moi thay doi document/index can duoc cap nhat trong entity/config/docs.

## Phase 0 - Design approval

Muc tieu: Chot ban ve truoc khi code.

Deliverables:

1. `product-requirements.md`
2. `business-rules.md`
3. `database-design.md`
4. `api-contract.md`
5. `security-design.md`
6. `learning-engine.md`
7. `development-roadmap.md`

Dieu kien xong:

1. Ban doc va dong y MVP scope.
2. Ban dong y role MVP.
3. Ban dong y API contract chinh.
4. Ban dong y database collection/index plan.

## Phase 1 - Project foundation

Muc tieu: Bien skeleton thanh nen mong backend san sang phat trien.

Cong viec:

1. Chuan hoa response va error model.
2. Chuan hoa config profiles: dev, test, prod.
3. Them CORS config.
4. Them Swagger Bearer JWT config.
5. Them Docker Compose cho MongoDB.
6. Them `.env.example`.
7. Them Actuator health neu can.

Deliverables:

1. Backend boot duoc voi DB local.
2. Swagger mo duoc.
3. MongoDB ket noi duoc, auto-index bat va seed data chay duoc.

## Phase 2 - Auth and Identity

Muc tieu: Hoan thanh dang ky, dang nhap va phan quyen co ban.

Cong viec:

1. Document/collection cho `users`, `roles`, `permissions`.
2. Document/collection cho `refresh_tokens`.
3. Seed role `ADMIN`, `CONTENT_MANAGER`, `LEARNER`.
4. Entity va repository identity.
5. Register API.
6. Login API.
7. Refresh token API.
8. Logout API.
9. Current user API.
10. JWT filter va security context.
11. Test auth service va controller.

Dieu kien xong:

1. User moi dang ky co role `LEARNER`.
2. Login tra access token va refresh token.
3. Token hop le goi duoc API private.
4. Token sai tra 401.
5. Role khong du quyen tra 403.

## Phase 3 - Content management

Muc tieu: Hoan thanh hoc lieu loi.

Cong viec:

1. Document/collection cho `languages`, `topics`, `tags`.
2. Document/collection cho `decks`, `flashcards`.
3. Seed languages va topics mau.
4. CRUD languages cho admin.
5. CRUD topics cho admin/content manager.
6. CRUD decks theo owner va role.
7. CRUD flashcards theo owner va role.
8. Public deck catalog.
9. Submit deck review.
10. Admin approve/reject deck.

Dieu kien xong:

1. Learner tao deck private duoc.
2. Learner them flashcard vao deck cua minh duoc.
3. Public catalog chi hien deck da duyet.
4. Admin duyet deck va ghi audit log.

## Phase 4 - Learning engine

Muc tieu: Hoan thanh phan hoc va on tap.

Cong viec:

1. Document/collection cho `study_sessions`, `review_items`, `review_logs`.
2. Document/collection cho `learning_progress`, `streaks`.
3. Start study session API.
4. Get reviews today API.
5. Submit review API.
6. Finish study session API.
7. Progress APIs.
8. Review scheduling service.
9. Streak update service.
10. Test thuat toan review.

Dieu kien xong:

1. User hoc deck va submit rating duoc.
2. `review_items` cap nhat dung interval.
3. `review_logs` ghi moi lan review.
4. `progress` va `streak` cap nhat dung.

## Phase 5 - Admin and observability

Muc tieu: Hoan thanh quan tri co ban va theo doi he thong.

Cong viec:

1. Dashboard admin.
2. Report content APIs.
3. Audit log list API.
4. Logging format co ban.
5. Swagger group theo module.
6. Cap nhat README chay backend.

Dieu kien xong:

1. Admin xem tong quan he thong.
2. Admin xem deck pending.
3. Admin xu ly report.
4. Swagger doc ro tung module.

## Phase 6 - Assessment

Muc tieu: Them quiz dua tren flashcard.

Cong viec:

1. Document/collection cho quiz.
2. Generate quiz tu deck.
3. Submit quiz attempt.
4. Score quiz.
5. Luu quiz attempt history.

Dieu kien xong:

1. User tao quiz tu deck duoc.
2. User submit dap an va nhan score.
3. Lich su attempt duoc luu.

## Phase 7 - Classroom

Muc tieu: Ho tro giao vien va lop hoc.

Cong viec:

1. Document/collection cho classroom.
2. Instructor tao class.
3. Add learner vao class.
4. Assign deck cho class.
5. Instructor xem progress cua learner.

Dieu kien xong:

1. Instructor quan ly class duoc.
2. Learner thay deck duoc assign.
3. Instructor xem tien do hoc vien.

## Thu tu uu tien neu thoi gian han che

Neu thoi gian do an it, chi can lam den Phase 4 la da co san pham loi tot.

Thu tu bat buoc:

1. Phase 0
2. Phase 1
3. Phase 2
4. Phase 3
5. Phase 4

Thu tu co the cat giam:

1. Phase 6 Assessment
2. Phase 7 Classroom
3. Notification
4. Advanced analytics

## Definition of done cho moi API

1. Co request DTO.
2. Co validation.
3. Co response DTO.
4. Co service logic.
5. Co repository/query can thiet.
6. Co authorization rule.
7. Co exception ro rang.
8. Co Swagger description.
9. Co test toi thieu cho logic quan trong.

## Rii ro va cach giam thieu

| Rii ro | Cach giam thieu |
|---|---|
| Scope qua rong | Chot MVP den Phase 4 truoc |
| Security phuc tap | Role-based truoc, permission-based sau |
| Learning algorithm kho test | Dung thuat toan MVP don gian |
| Database thay doi nhieu | Chot collection/index plan truoc khi code |
| Swagger thieu ro rang | Viet API contract truoc |
| Frontend chua co | Swagger va Postman co the test backend |

## Checklist truoc khi bat dau code

1. Ban da doc `product-requirements.md`.
2. Ban da dong y role MVP trong `business-rules.md`.
3. Ban da dong y collection design trong `database-design.md`.
4. Ban da dong y API trong `api-contract.md`.
5. Ban da dong y JWT/refresh token trong `security-design.md`.
6. Ban da dong y thuat toan review trong `learning-engine.md`.
7. Ban da dong y thu tu phase trong file nay.
