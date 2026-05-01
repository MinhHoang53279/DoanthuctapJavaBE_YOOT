# Learning Engine Design

## Muc tieu

Learning Engine la phan loi cua de tai. Muc tieu la giup nguoi hoc on tap flashcard dung thoi diem dua tren muc do ghi nho.

## Khai niem chinh

| Khai niem | Mo ta |
|---|---|
| Flashcard | Mot the hoc trong deck |
| Study Session | Mot phien hoc cua user voi deck |
| Review Item | Trang thai ghi nho cua user voi mot flashcard |
| Review Log | Lich su tung lan user review |
| Mastery Level | Muc do thanh thao cua user voi flashcard |
| Next Review | Thoi diem nen on lai card |

## Luong hoc tong quat

```text
User chon deck
Backend tao study session
Backend lay card moi va card den han review
User hoc tung card
User chon rating AGAIN/HARD/GOOD/EASY
Backend cap nhat review item
Backend ghi review log
Backend cap nhat progress va streak
```

## Rating model

| Rating | Score | Tac dong |
|---|---:|---|
| AGAIN | 0 | Reset hoac giam manh interval |
| HARD | 3 | Tang interval cham |
| GOOD | 4 | Tang interval binh thuong |
| EASY | 5 | Tang interval nhanh |

## Mastery level

| Level | Dieu kien de xuat |
|---|---|
| NEW | Chua review lan nao |
| LEARNING | Da review nhung interval nho hon 7 ngay |
| REVIEWING | Interval tu 7 den duoi 30 ngay |
| MASTERED | Interval tu 30 ngay tro len va repetition_count >= 5 |

## Review item fields

| Field | Y nghia |
|---|---|
| ease_factor | He so de/nho cua card doi voi user |
| interval_days | So ngay den lan review tiep theo |
| repetition_count | So lan review thanh cong lien tiep |
| mastery_level | Muc do thanh thao hien tai |
| last_review_at | Lan review gan nhat |
| next_review_at | Lan can review tiep theo |

## Thuat toan MVP

Thuat toan nen don gian, de giai thich trong do an va de test.

### Gia tri mac dinh

| Field | Default |
|---|---:|
| ease_factor | 2.50 |
| interval_days | 0 |
| repetition_count | 0 |
| mastery_level | NEW |

### Quy tac tinh interval

Neu user chon `AGAIN`:

1. `repetition_count = 0`
2. `interval_days = 0`
3. `ease_factor = max(1.30, ease_factor - 0.20)`
4. `next_review_at = now + 10 minutes` hoac `now + 1 day` tuy mode
5. `mastery_level = LEARNING`

Neu user chon `HARD`:

1. `repetition_count = repetition_count + 1`
2. `interval_days = max(1, round(interval_days * 1.2))`
3. `ease_factor = max(1.30, ease_factor - 0.15)`
4. `next_review_at = now + interval_days`

Neu user chon `GOOD`:

1. `repetition_count = repetition_count + 1`
2. Neu `repetition_count == 1`, `interval_days = 1`
3. Neu `repetition_count == 2`, `interval_days = 3`
4. Neu lon hon 2, `interval_days = round(interval_days * ease_factor)`
5. `ease_factor` giu nguyen hoac tang nhe `+0.05`

Neu user chon `EASY`:

1. `repetition_count = repetition_count + 1`
2. Neu card moi, `interval_days = 4`
3. Neu khong, `interval_days = round(interval_days * ease_factor * 1.3)`
4. `ease_factor = ease_factor + 0.15`

## Chon card cho study session

Thu tu uu tien khi bat dau session:

1. Card da den han review: `next_review_at <= now`.
2. Card dang learning co interval ngan.
3. Card moi chua co review item.
4. Gioi han theo `limit` request, mac dinh 20.

Query logic:

```text
Lay active flashcards trong deck
Left join review_items theo current user
Uu tien review_items.next_review_at <= now
Sau do den flashcards chua co review_items
Sap xep theo next_review_at ASC, card_order ASC
Limit N
```

## Study session status

| Status | Y nghia |
|---|---|
| IN_PROGRESS | Dang hoc |
| COMPLETED | Da ket thuc binh thuong |
| ABANDONED | Bo do hoac het han |

## Progress calculation

### learned_cards

So card trong deck co `review_items.repetition_count > 0` hoac co `review_logs`.

### mastered_cards

So card trong deck co `mastery_level = MASTERED`.

### completion_rate

```text
completion_rate = learned_cards / total_active_cards * 100
```

Neu deck khong co active card thi completion rate bang 0.

## Streak calculation

Streak duoc cap nhat khi user co review log hop le.

Quy tac:

1. Neu `last_study_date` null, current streak = 1.
2. Neu `last_study_date` la hom qua, current streak + 1.
3. Neu `last_study_date` la hom nay, giu current streak.
4. Neu cach hon 1 ngay, reset current streak = 1.
5. `best_streak_days = max(best_streak_days, current_streak_days)`.

## Review log append-only

`review_logs` khong nen update sau khi tao, tru khi can fix data bang admin maintenance. Ly do:

1. Bao toan lich su hoc.
2. De tinh lai progress neu can.
3. De phan tich chat luong hoc sau nay.

## API lien quan

| API | Muc dich |
|---|---|
| `POST /study-sessions/start` | Tao phien hoc va lay cards |
| `POST /reviews/{flashcardId}` | Submit ket qua review |
| `POST /study-sessions/{id}/finish` | Ket thuc phien hoc |
| `GET /reviews/today` | Lay card can on hom nay |
| `GET /progress/me` | Lay tong quan tien do |
| `GET /progress/decks/{deckId}` | Lay tien do cua deck |

## Edge cases can xu ly

1. Deck khong co flashcard active.
2. User hoc deck khong co quyen truy cap.
3. Flashcard da bi xoa mem sau khi session bat dau.
4. User submit review 2 lan qua nhanh cho cung card.
5. Study session da completed nhung user van submit review.
6. User doi timezone lam streak lech ngay.

## Test cases quan trong

1. Card moi + GOOD -> interval 1 ngay.
2. Card moi + EASY -> interval 4 ngay.
3. Card dang reviewing + AGAIN -> reset repetition.
4. Card due today xuat hien trong `/reviews/today`.
5. Card chua den han khong xuat hien trong `/reviews/today`.
6. Submit review tao `review_log` moi.
7. Submit review cap nhat `learning_progress`.
8. Review ngay lien tiep cap nhat streak.

## Diem co the nang cap sau MVP

1. Ap dung SM-2 chuan hon.
2. Them daily learning goal.
3. Them reminder notification.
4. Them analytics theo do kho cua card.
5. De xuat deck/card dua tren lich su hoc.
