# 1) Stage: Compilación con Maven
FROM maven:3.9-jdk-22 AS builder
WORKDIR /app
COPY . .
RUN mvn -B clean package spring-boot:repackage

# 2) Stage: Runtime con OpenJDK
FROM openjdk:22
WORKDIR /tmp/

# Si necesitas cargar variables de entorno desde .env dentro del contenedor
COPY .env ./

# Copia el JAR generado en el builder
COPY --from=builder /app/target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar

# Puerto expuesto
EXPOSE 8080

# Entrada de la aplicación
ENTRYPOINT ["java","-jar","MyTodoList.jar"]