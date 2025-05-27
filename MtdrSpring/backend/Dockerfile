FROM openjdk:22

WORKDIR /tmp/

COPY .env ./

EXPOSE 8080

COPY target/MyTodoList-0.0.1-SNAPSHOT.jar MyTodoList.jar
ENTRYPOINT ["java","-jar","MyTodoList.jar"]
