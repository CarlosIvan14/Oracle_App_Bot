# ---------- Etapa 1: build del frontend (React) ----------
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

COPY src/main/frontend/package*.json ./
RUN npm ci

COPY src/main/frontend ./
RUN npm run build

# ---------- Etapa 2: build del backend (Spring Boot) ----------
FROM maven:3.9-eclipse-temurin-22-alpine AS backend-build
WORKDIR /app

# Descarga deps offline
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Código backend + assets del frontend ya compilado
COPY . ./
RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/build src/main/resources/static

# Empaqueta JAR
RUN mvn -B clean package spring-boot:repackage

# ---------- Etapa 3: runtime ligero ----------
FROM openjdk:22-jdk-slim
WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
