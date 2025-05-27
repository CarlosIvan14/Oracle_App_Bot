# 1) Etapa de compilación con Maven
FROM maven:3.9-jdk-22 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2) Etapa final con sólo el JAR y OpenJDK
FROM openjdk:22
WORKDIR /tmp/
COPY .env ./
COPY --from=builder /app/target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","MyTodoList.jar"]
