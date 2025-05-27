# ---------- Etapa 1: build con Maven ----------
FROM maven:3.9-eclipse-temurin-22-alpine AS builder
WORKDIR /app

# 1) Cacha dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2) Copia el código fuente y el .env
COPY . .
COPY .env ./

# 3) Compila, saltando la descarga de Node/NPM
#    -Dskip.installnodenpm evita install-node-and-npm :contentReference[oaicite:0]{index=0}
#    -Dskip.npm evita ejecuciones de npm :contentReference[oaicite:1]{index=1}
RUN mvn -B \
    -Dmaven.multiModuleProjectDirectory=/app \
    -Dskip.installnodenpm=true \
    -Dskip.npm=true \
    clean package spring-boot:repackage

# ---------- Etapa 2: runtime ligero ----------
FROM openjdk:22-jdk-slim
WORKDIR /tmp/

# Copia .env si tu app la necesita en runtime
COPY .env ./

# Copia el JAR generado (cualquier SNAPSHOT.jar)
COPY --from=builder /app/target/*.jar MyTodoList.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","MyTodoList.jar"]
