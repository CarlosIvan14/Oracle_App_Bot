FROM openjdk:11-jre-slim

WORKDIR /app

# Copy the JAR file first (better layer caching)
COPY target/MyTodoList-*.jar app.jar

# Copy configuration files
COPY .env .

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]