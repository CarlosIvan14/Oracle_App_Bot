FROM openjdk:22

WORKDIR /tmp/

# Expose port 8080
EXPOSE 8080

# Copy the application JAR and the .env file
COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
COPY .env .env

# Start the application
ENTRYPOINT ["java","-jar","MyTodoList.jar"]
