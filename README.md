# Flashcard Learning Platform

Du an duoc tach theo huong full project, trong do backend Spring Boot nam trong thu muc `backend`.

## Cau truc

- `backend/`: ma nguon backend Spring Boot
- `docs/erd.md`: mo ta collection va quan he du lieu
- `docs/architecture.md`: kien truc module, package va roadmap coding

## Chay backend

1. Tao file `.env` tu `.env.example` va thay gia tri secret/password that.
2. Khoi dong MongoDB local neu can:

```bash
docker compose --env-file .env up -d mongodb
```

3. Chay backend:

```bash
cd backend
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

Health check:

```text
http://localhost:8080/actuator/health
```

## Cau hinh moi truong

Backend doc cac bien moi truong sau:

- `MONGODB_URI`
- `MONGODB_TEST_URI`
- `JWT_SECRET`
- `ACCESS_TOKEN_EXPIRATION_MS`
- `REFRESH_TOKEN_EXPIRATION_MS`
- `CORS_ALLOWED_ORIGINS`

Khong commit file `.env` that. File `.env.example` chi dung lam mau.

## Kiem tra nhanh

```bash
cd backend
mvn -q -DskipTests compile
mvn test
```
