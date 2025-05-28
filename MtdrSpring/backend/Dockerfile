# Etapa 1: build del frontend (React)
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend
COPY src/main/frontend/package*.json ./
RUN npm ci
COPY src/main/frontend ./
RUN npm run build

# Etapa 2: build del backend (Spring Boot)
FROM maven:3.9-eclipse-temurin-22-alpine AS backend-build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY . ./
RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/build src/main/resources/static
# aquí saltamos cualquier ejecución de frontend-maven-plugin
RUN mvn -Dskip.installnodenpm=true -Dskip.npm=true -B clean package spring-boot:repackage

# Etapa 3: runtime ligero con .env
FROM openjdk:22-jdk-slim
WORKDIR /app
COPY .env ./
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
