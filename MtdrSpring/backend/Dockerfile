FROM maven:3.8.6-amazoncorretto-11 as build
WORKDIR /build
COPY . .
RUN mvn clean package spring-boot:repackage

FROM amazoncorretto:11-alpine-jdk
WORKDIR /app
COPY --from=build /build/target/MyTodoList-*.jar app.jar
COPY .env .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
