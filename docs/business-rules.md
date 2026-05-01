# Business Rules

## Role he thong

| Role | Mo ta | Giai doan |
|---|---|---|
| SUPER_ADMIN | Toan quyen he thong | Sau MVP |
| ADMIN | Quan tri user, content, dashboard | MVP |
| CONTENT_MANAGER | Quan ly hoc lieu public | MVP |
| INSTRUCTOR | Quan ly lop va gan deck | Sau MVP |
| LEARNER | Hoc flashcard va tao deck rieng | MVP |
| MODERATOR | Kiem duyet noi dung bi report | Sau MVP |

## Trang thai user

| Status | Y nghia |
|---|---|
| ACTIVE | Duoc dang nhap va su dung binh thuong |
| LOCKED | Bi khoa tam thoi boi admin |
| DISABLED | Bi vo hieu hoa dai han |

## Luat auth

1. Email va username la duy nhat.
2. Password luu bang BCrypt, khong bao gio luu plain text.
3. User `LOCKED` hoac `DISABLED` khong duoc dang nhap.
4. Refresh token chi dung mot user va co han su dung.
5. Logout phai revoke refresh token hien tai.
6. Access token ngan han, refresh token dai han hon.

## Luat content

1. Moi deck phai co title.
2. Moi deck nen co source language va target language.
3. Moi deck co mot creator.
4. Learner chi sua/xoa deck do minh tao va chua bi khoa.
5. Content Manager va Admin co the tao deck public.
6. Deck public can co status `PENDING` truoc khi thanh `APPROVED`.
7. Deck `REJECTED` khong hien thi tren public catalog.
8. Xoa deck nen la soft delete neu deck da co lich su hoc.
9. Moi flashcard chi thuoc mot deck.
10. Flashcard can co `front_text` va `back_text`.

## Trang thai deck

| Status | Y nghia |
|---|---|
| DRAFT | Dang soan thao |
| PENDING | Cho duyet cong khai |
| APPROVED | Da duyet va co the cong khai |
| REJECTED | Bi tu choi |

## Visibility deck

| Visibility | Y nghia |
|---|---|
| PRIVATE | Chi creator thay va hoc |
| PUBLIC | Co the xuat hien trong catalog sau khi duyet |

## Luat learning

1. Moi user co trang thai hoc rieng cho tung flashcard.
2. Cap `user_id + flashcard_id` trong `review_items` la duy nhat.
3. Card moi chua co `review_item` se duoc khoi tao khi user bat dau hoc.
4. Moi lan user danh gia card phai tao mot `review_log`.
5. `review_log` khong nen sua sau khi tao.
6. `next_review_at` quyet dinh card nao xuat hien trong danh sach on tap hom nay.
7. `learning_progress` duy nhat theo cap `user_id + deck_id`.
8. `streak` chi tang khi user co it nhat mot review hop le trong ngay.

## Luat review score

| Rating | Score | Y nghia |
|---|---:|---|
| AGAIN | 0 | Quen, can hoc lai som |
| HARD | 3 | Nho kho, can on lai gan |
| GOOD | 4 | Nho on, tang interval binh thuong |
| EASY | 5 | Nho rat tot, tang interval nhanh |

## Luat progress

1. `learned_cards` la so card user da review it nhat mot lan.
2. `mastered_cards` la so card co mastery level dat nguong `MASTERED`.
3. `completion_rate = learned_cards / total_active_cards * 100`.
4. Deck khong co card active thi completion rate bang 0.
5. Progress cap nhat sau moi review hop le.

## Luat admin

1. Admin khong duoc tu xoa tai khoan cua chinh minh neu la admin duy nhat.
2. Khoa user phai ghi audit log.
3. Duyet deck public phai ghi actor va thoi diem duyet.
4. Tu choi deck nen co ly do.
5. Bao cao noi dung can co status xu ly.

## Luat audit

Can ghi audit log cho cac hanh dong:

1. Admin khoa/mo khoa user.
2. Admin duyet/tu choi deck.
3. Admin cap role cho user.
4. User dang nhap that bai nhieu lan neu co rate limit.
5. Xoa hoac archive deck co nhieu nguoi hoc.

## Luat phan trang va tim kiem

1. Danh sach user, deck, flashcard, report can phan trang.
2. Page mac dinh la 0.
3. Size mac dinh la 20.
4. Size toi da nen la 100.
5. Search text nen trim va gioi han do dai.
