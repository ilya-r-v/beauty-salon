# Beauty Salon Spring backend

Java 21, Maven, PostgreSQL, Flyway. Из каталога backend:

```sh
cp .env.example .env
# задайте DB_PASSWORD, при занятом 5432 задайте DB_PORT=55432
make up
```

Flyway автоматически применяет `src/main/resources/db/migration` перед Hibernate validate.
Health: http://localhost:8080/actuator/health. Swagger: /swagger-ui.html (ADMIN).
`make down` сохраняет данные PostgreSQL.

Для существующей БД, ранее созданной Go migrate, сначала сделайте резервную копию,
проверьте соответствие схемы миграциям V1–V6 и вручную выполните Flyway baseline
с baselineVersion=6. Автоматический baseline намеренно отключён: он может скрыть
несовпадение схемы. Старые SQL в migrations оставлены как история переноса.

Локально: Java 21, `bash mvnw test`, `bash mvnw spring-boot:run`.
Настройки БД передаются переменными DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD.