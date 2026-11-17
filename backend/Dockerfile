FROM node:20-alpine AS web
WORKDIR /web
COPY --from=frontend_src package*.json ./
RUN npm ci --no-audit --no-fund
COPY --from=frontend_src . .
RUN npm run build

FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
COPY --from=web /web/dist ./src/main/resources/static
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=builder /app/target/*.jar app.jar   

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]