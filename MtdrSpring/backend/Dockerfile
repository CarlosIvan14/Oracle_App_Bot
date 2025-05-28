# ---------- Etapa 1: build del frontend React ----------
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

# Copia sólo package.json para cachear npm install
COPY frontend/package*.json ./
RUN npm ci

# Copia el resto del código y genera los archivos estáticos
COPY frontend ./
RUN npm run build

# ---------- Etapa 2: build de la aplicación Spring Boot ----------
FROM maven:3.9-eclipse-temurin-22-alpine AS backend-build
WORKDIR /app

# Cachear dependencias de Maven
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copia todo el código del backend
COPY . .

# Inyecta los archivos estáticos de React dentro de src/main/resources/static
# (Spring Boot los servirá automáticamente)
COPY --from=frontend-build /app/frontend/build ./src/main/resources/static

# Construye el JAR final
RUN mvn -B clean package spring-boot:repackage

# ---------- Etapa 3: runtime ligero ----------
FROM openjdk:22-jdk-slim
WORKDIR /app

# Copia el JAR generado
COPY --from=backend-build /app/target/*.jar app.jar

# Si tu aplicación necesita .env en runtime:
COPY .env ./

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]