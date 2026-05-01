# Product Requirements

## Ten san pham

Flashcard Learning Platform Backend.

## Mo ta ngan

He thong backend API ho tro nguoi hoc ngoai ngu bang flashcard, lich on tap thong minh va theo doi tien do hoc tap.

## Boi canh doanh nghiep

Doanh nghiep gia lap la mot nen tang edtech cung cap hoc lieu ngon ngu cho hoc vien, giao vien va doi ngu quan tri noi dung.

## Muc tieu san pham

1. Cho phep hoc vien hoc tu vung/cau mau bang flashcard.
2. Cho phep tao, quan ly va duyet bo deck flashcard.
3. Ghi nhan qua trinh hoc va tinh lich on tap tiep theo.
4. Ho tro quan tri nguoi dung, noi dung va bao cao co ban.
5. Cung cap API ro rang qua Swagger de frontend tich hop.

## Doi tuong su dung

| Vai tro | Mo ta |
|---|---|
| Learner | Hoc vien hoc flashcard, on tap, xem tien do |
| Content Manager | Bien tap noi dung hoc lieu, deck, flashcard |
| Instructor | Tao lop, gan deck, theo doi hoc vien |
| Admin | Quan ly user, noi dung, bao cao |
| Super Admin | Toan quyen he thong |

## Pham vi MVP

MVP can hoan thanh cac nhom chuc nang sau:

| Nhom | Chuc nang |
|---|---|
| Auth | Dang ky, dang nhap, refresh token, logout, current user |
| Identity | User, profile, role co ban |
| Content | Language, topic, deck, flashcard |
| Learning | Study session, review item, review log, progress |
| Admin | Dashboard co ban, duyet deck, khoa user |
| Docs | Swagger UI va API contract ro rang |

## Ngoai pham vi MVP

| Chuc nang | Ly do de sau |
|---|---|
| Payment | Khong can cho do an giai doan dau |
| Microservices | He thong chua du lon |
| Eureka | Khong can service discovery |
| AI generation | Nang cao, khong phai loi nghiep vu |
| Mobile app | Backend truoc, client sau |
| Real-time chat | Khong lien quan truc tiep den flashcard learning |

## Yeu cau chuc nang

### Auth

1. Nguoi dung co the dang ky tai khoan bang email, username va password.
2. Nguoi dung co the dang nhap de nhan access token va refresh token.
3. Nguoi dung co the lam moi access token bang refresh token hop le.
4. Nguoi dung co the dang xuat, refresh token bi revoke.
5. API rieng tu yeu cau JWT hop le.

### Identity

1. User co profile rieng.
2. User co trang thai `ACTIVE`, `LOCKED`, `DISABLED`.
3. User co mot hoac nhieu role.
4. Admin co the khoa/mo khoa user.

### Content

1. He thong quan ly danh sach ngon ngu hoc.
2. He thong quan ly chu de hoc tap.
3. User co the tao deck rieng.
4. Content Manager co the tao deck public.
5. Deck gom nhieu flashcard.
6. Flashcard co front text, back text, phat am, vi du va ghi chu.
7. Deck public can duoc duyet truoc khi hien thi cong khai.

### Learning

1. Learner co the bat dau mot study session voi deck.
2. He thong tra ve card moi va card den han on tap.
3. Learner danh gia ket qua nho bang `AGAIN`, `HARD`, `GOOD`, `EASY`.
4. He thong cap nhat lich on tap tiep theo.
5. He thong ghi log moi lan review.
6. He thong cap nhat tien do deck va streak hoc tap.

### Admin

1. Admin xem tong so user, deck, flashcard va session.
2. Admin duyet hoac tu choi deck public.
3. Admin xem report noi dung.
4. Cac thao tac quan tri quan trong can ghi audit log.

## Yeu cau phi chuc nang

| Nhom | Yeu cau |
|---|---|
| Security | JWT, BCrypt, role-based authorization |
| Maintainability | Modular monolith, module boundary ro rang |
| Data integrity | Flyway migration, constraint ro rang |
| Observability | Logging, health check, Swagger |
| Performance | Pagination, index cho query quan trong |
| Testability | Unit test va integration test cho module loi |

## Dieu kien nghiem thu MVP

1. Backend chay duoc bang Maven.
2. Swagger hien thi day du API MVP.
3. Auth register/login/refresh/logout hoat dong.
4. CRUD deck va flashcard hoat dong theo quyen.
5. Learning review cap nhat duoc `review_items` va `review_logs`.
6. Progress cua learner duoc tinh theo deck.
7. Migration tao duoc schema tren PostgreSQL sach.
8. Co seed role va permission co ban.
