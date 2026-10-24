# Запуск проекта через Make

### Быстрый запуск в Docker (рекомендуемый)

1. **Собрать и поднять весь проект:**
```bash
make up

```


*(Команда соберет образ бэкенда и запустит контейнеры в фоне)*
2. **Применить миграции базы данных:**
```bash
make migrate-up

```


3. **Смотреть логи бэкенда в реальном времени:**
```bash
make logs

```


4. **Остановить проект:**
```bash
make down

```



---

### Локальный запуск (для разработки)

1. **Запустить только базу данных:**
```bash
make db

```


2. **Применить миграции:**
```bash
make migrate-up

```


3. **Запустить приложение Spring Boot:**
```bash
./mvnw spring-boot:run

```


*(на Windows: `mvnw.cmd spring-boot:run`)*

---

### Дополнительные команды для миграций

* **Откатить последнюю миграцию:**
```bash
make migrate-down

```


* **Проверить текущую версию схемы БД:**
```bash
make migrate-version

```


* **Создать новую миграцию:**
```bash
make migrate-create name=add_new_table

```



---

### Доступ к Swagger UI

После запуска перейдите в браузере:

* **Интерактивная документация:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
*(или прямой путь к SPA: `http://localhost:8080/swagger-ui/index.html`)*
* **Спецификация OpenAPI (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **Авторизация в Swagger:**
> 1. Вызовите `POST /api/auth/register` или `POST /api/auth/login`.
> 2. Скопируйте полученный `token` из ответа.
> 3. Нажмите кнопку **Authorize 🔓** в правом верхнем углу интерфейса Swagger и вставьте токен (без префикса `Bearer`).
> 
>