# ---------- Etapa 1: build del frontend (React) ----------
FROM node:18-alpine AS frontend-build
WORKDIR /app/frontend

# Copiamos solo package.json para caché de dependencias
COPY src/main/frontend/package*.json ./
RUN npm ci

# Copiamos el resto y construimos
COPY src/main/frontend ./
RUN npm run build

# ---------- Etapa 2: build del backend (Spring Boot) ----------
FROM maven:3.9-eclipse-temurin-22-alpine AS backend-build
WORKDIR /app

# Caché de dependencias Maven
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copiamos todo el código backend
COPY . ./

# Inyectamos el build del frontend en recursos estáticos
RUN mkdir -p src/main/resources/static
COPY --from=frontend-build /app/frontend/build src/main/resources/static

# Empaquetamos todo en un JAR ejecutable
RUN mvn -B clean package spring-boot:repackage

# ---------- Etapa 3: runtime ligero ----------
FROM openjdk:22-jdk-slim
WORKDIR /app

# Solo llevamos el JAR ya empacado
COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
