FROM maven:3.9.6-eclipse-temurin-21 AS base
WORKDIR /app
COPY pom.xml .

FROM base AS development
RUN mkdir -p /wallet
COPY wallet/ /wallet
EXPOSE 8081
ENTRYPOINT ["mvn", "spring-boot:run"]

# Build image, creates the production JAR
FROM base AS build
COPY src ./src
COPY .env ./
RUN mvn clean package -DskipTests

# Production image
FROM openjdk:22 AS production
WORKDIR /app
COPY --from=build /app/target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "MyTodoList.jar"]