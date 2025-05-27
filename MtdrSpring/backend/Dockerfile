# 1) Stage: Compilación con Maven (Eclipse Temurin JDK22 sobre Alpine) e instalación de Node
FROM maven:3.9-eclipse-temurin-22-alpine AS builder
WORKDIR /app

# Copia sólo pom.xml para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el resto del código
COPY src ./src
COPY .env ./

# Instala Node.js y npm para el frontend-maven-plugin
RUN apk add --no-cache nodejs npm

# Compila el paquete completo (incluye frontend si aplica)
RUN mvn -B clean package spring-boot:repackage

# 2) Stage: Runtime con OpenJDK 22 (slim)
FROM openjdk:22-jdk-slim
WORKDIR /tmp/

# Copia variables de entorno (sólo si es necesario dentro del contenedor)
COPY --from=builder /app/.env ./

# Copia el JAR generado en el builder
COPY --from=builder /app/target/*SNAPSHOT.jar MyTodoList.jar

# Puerto expuesto
EXPOSE 8080

# Entrada de la aplicación
ENTRYPOINT ["java","-jar","MyTodoList.jar"]