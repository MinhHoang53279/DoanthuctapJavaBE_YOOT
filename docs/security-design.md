# Security Design

## Muc tieu bao mat

1. Bao ve API rieng tu bang JWT.
2. Dam bao password khong bao gio luu plain text.
3. Ho tro phan quyen theo role.
4. Quan ly refresh token co the revoke.
5. Ghi audit log cho hanh dong quan tri quan trong.
6. Giam nguy co ro ri thong tin loi he thong.

## Thanh phan security

| Thanh phan | Cong nghe de xuat |
|---|---|
| Authentication | Spring Security |
| Password hashing | BCrypt |
| Access token | JWT |
| Refresh token | Random opaque token, hash luu DB |
| Authorization | Role-based, mo rong permission-based |
| API docs auth | Swagger Bearer JWT |

## Token strategy

### Access token

| Thuoc tinh | Gia tri de xuat |
|---|---|
| Format | JWT |
| Expiration | 15 den 60 phut |
| Storage client | Memory hoac secure storage tuy frontend |
| Payload | userId, username, roles |
| Purpose | Goi API private |

### Refresh token

| Thuoc tinh | Gia tri de xuat |
|---|---|
| Format | Random string khong phai JWT |
| Expiration | 7 den 30 ngay |
| Storage DB | Hash token |
| Revoke | Co |
| Rotation | Co, moi lan refresh cap token moi |

## JWT claims de xuat

```json
{
  "sub": "1",
  "username": "learner01",
  "roles": ["LEARNER"],
  "iat": 1777024800,
  "exp": 1777028400
}
```

Khong dua thong tin nhay cam vao JWT.

## Auth flow

### Register flow

```text
Client -> POST /auth/register
Backend validate input
Backend check email/username unique
Backend hash password bang BCrypt
Backend tao user ACTIVE
Backend gan role LEARNER
Backend tao user profile rong
Backend tra ve user summary
```

### Login flow

```text
Client -> POST /auth/login
Backend tim user theo email hoac username
Backend check status ACTIVE
Backend verify password
Backend tao access token
Backend tao refresh token va luu hash vao DB
Backend tra token pair
```

### Refresh flow

```text
Client -> POST /auth/refresh
Backend hash refresh token input
Backend tim token trong DB
Backend check revoked=false va chua het han
Backend revoke token cu
Backend tao access token moi
Backend tao refresh token moi
Backend tra token pair moi
```

### Logout flow

```text
Client -> POST /auth/logout
Backend validate current user
Backend hash refresh token
Backend revoke token neu thuoc current user
Backend tra 204
```

## Authorization matrix MVP

| API group | Public | Learner | Content Manager | Admin |
|---|---|---|---|---|
| `/auth/register` | Yes | Yes | Yes | Yes |
| `/auth/login` | Yes | Yes | Yes | Yes |
| `/auth/refresh` | Yes | Yes | Yes | Yes |
| `/auth/me` | No | Yes | Yes | Yes |
| `/users` | No | No | No | Yes |
| `/users/me/profile` | No | Yes | Yes | Yes |
| `/languages GET` | Optional | Yes | Yes | Yes |
| `/languages POST` | No | No | No | Yes |
| `/topics GET` | Optional | Yes | Yes | Yes |
| `/topics POST` | No | No | Yes | Yes |
| `/decks POST` | No | Yes | Yes | Yes |
| `/decks GET` | Optional | Yes | Yes | Yes |
| `/flashcards POST` | No | Owner | Yes | Yes |
| `/study-sessions` | No | Yes | Yes | Yes |
| `/reviews` | No | Yes | Yes | Yes |
| `/admin` | No | No | Partial | Yes |

## Permission naming

Nen dat permission theo pattern:

```text
RESOURCE_ACTION
```

Vi du:

1. `USER_READ`
2. `USER_MANAGE_STATUS`
3. `DECK_CREATE`
4. `DECK_APPROVE`
5. `FLASHCARD_UPDATE_OWN`
6. `LEARNING_REVIEW`
7. `ADMIN_DASHBOARD_READ`

MVP co the check role truoc. Permission chi can seed va thiet ke san de mo rong.

## Endpoint public

Cho phep khong can token:

1. `/api/v1/auth/register`
2. `/api/v1/auth/login`
3. `/api/v1/auth/refresh`
4. `/swagger-ui/**`
5. `/swagger-ui.html`
6. `/v3/api-docs/**`
7. `/actuator/health` neu them Actuator

## CORS policy

Moi truong dev:

```text
http://localhost:3000
http://localhost:5173
```

Moi truong prod:

1. Chi allow domain frontend that.
2. Khong dung wildcard `*` khi co Authorization header.
3. Allow methods: GET, POST, PUT, PATCH, DELETE, OPTIONS.
4. Allow headers: Authorization, Content-Type.

## Password policy

MVP:

1. Toi thieu 8 ky tu.
2. Khong chap nhan password blank.
3. Trim input username/email, khong trim password.

Nang cao:

1. Yeu cau chu hoa, chu thuong, so, ky tu dac biet.
2. Lock tam thoi khi login fail nhieu lan.
3. Email verification.
4. Forgot password.

## Error handling security

1. Login fail nen tra message chung: `Invalid username/email or password`.
2. Khong tra stack trace cho client.
3. Loi 500 khong nen tra `ex.getMessage()` trong production.
4. Validation error co the tra field message.
5. 403 phai ro la khong du quyen, khong tiet lo resource nhay cam.

## Audit security

Can audit cac hanh dong:

1. Login fail nhieu lan neu co rate limit.
2. Admin khoa/mo khoa user.
3. Admin gan role.
4. Admin duyet/tu choi deck.
5. Admin resolve report.
6. Xoa deck co lich su hoc.

## Environment variables

Khong hardcode cac gia tri sau:

| Bien | Mo ta |
|---|---|
| `DB_URL` | JDBC URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret ky JWT |
| `ACCESS_TOKEN_EXPIRATION_MS` | Han access token |
| `REFRESH_TOKEN_EXPIRATION_MS` | Han refresh token |
| `CORS_ALLOWED_ORIGINS` | Frontend origins |

## Checklist truoc khi code security

1. Chot role MVP.
2. Chot access token expiration.
3. Chot refresh token expiration.
4. Chot format response khi login fail.
5. Chot endpoint nao public.
6. Chot co rotation refresh token hay khong. De xuat: co.
