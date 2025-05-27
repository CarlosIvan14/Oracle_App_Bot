# 1) Stage: Compilación con Maven (JDK 22 sobre Alpine)
FROM maven:3.9-eclipse-temurin-22-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn -B clean package spring-boot:repackage

# 2) Stage: Runtime con OpenJDK 22
FROM openjdk:22
WORKDIR /tmp/

# Copia variables de entorno, si las usas dentro del contenedor
COPY .env ./

# Copia el JAR generado en el builder
COPY --from=builder /app/target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar

# Puerto expuesto
EXPOSE 8080

# Entrada de la aplicación
ENTRYPOINT ["java","-jar","MyTodoList.jar"]