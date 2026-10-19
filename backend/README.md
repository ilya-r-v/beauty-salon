# Beauty Salon — Spring Boot Backend (Auth & Registration)

Микросервис авторизации и регистрации на базе **Spring Boot 3 + Spring Security + JWT + JDBC**.

## Архитектура

- **Доступ к данным**: Прямой **JDBC** через `javax.sql.DataSource` (пул соединений HikariCP).
- **Хранилище**: PostgreSQL (таблицы `client` и `role`).
- **Хеширование паролей**: Алгоритм `PBKDF2WithHmacSHA256` с солью (100% совместим с базой данных и JavaFX клиентом).
- **Безопасность**: Spring Security Stateless + JWT Bearer токены (HMAC-SHA256).

## Эндпоинты REST API

### 1. Регистрация нового пользователя
`POST /api/auth/register` (доступно без авторизации)

**Тело запроса (JSON):**
```json
{
  "name": "Иван Иванов",
  "phone": "+79991234567",
  "password": "secretpassword",
  "passwordConfirm": "secretpassword"
}
```

**Ответ (201 Created):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "Иван Иванов",
  "phone": "+79991234567",
  "role": "CLIENT"
}
```

---

### 2. Вход в систему (Аутентификация)
`POST /api/auth/login` (доступно без авторизации)

**Тело запроса (JSON):**
```json
{
  "phone": "+79001112235",
  "password": "salon123"
}
```

**Ответ (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "id": "a1111111-0000-0000-0000-000000000003",
  "name": "Анна Сидорова",
  "phone": "+79001112235",
  "role": "CLIENT"
}
```

---

### 3. Получение профиля текущего пользователя
`GET /api/auth/me` (требует JWT-токен)

**Заголовок:** `Authorization: Bearer <token>`

**Ответ (200 OK):**
```json
{
  "id": "a1111111-0000-0000-0000-000000000003",
  "name": "Анна Сидорова",
  "phone": "+79001112235",
  "role": "CLIENT"
}
```

---

### 4. Проверка работоспособности
`GET /api/health`

**Ответ (200 OK):**
```json
{
  "status": "UP"
}
```

## Запуск через Docker Compose

Из корня проекта:
```bash
docker compose up -d --build
```

Остановка:
```bash
docker compose down
```
