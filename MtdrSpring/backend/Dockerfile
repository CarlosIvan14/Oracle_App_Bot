# Base image for development
FROM maven:3.9.6-eclipse-temurin-21 AS development
WORKDIR /app
COPY pom.xml .
RUN mkdir -p /wallet
COPY wallet/ /wallet
EXPOSE 8080

# Production image - simplified approach
FROM openjdk:22 AS production
WORKDIR /app
COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
COPY .env ./
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "MyTodoList.jar"]