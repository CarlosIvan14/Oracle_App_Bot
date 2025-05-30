FROM mx-queretaro-1.ocir.io/axm8kysebnqa/reacttodo/s78fu/base-images/maven:3.8.6-amazoncorretto-11 as build
WORKDIR /build
COPY . .
RUN mvn clean package spring-boot:repackage

FROM mx-queretaro-1.ocir.io/axm8kysebnqa/reacttodo/s78fu/base-images/openjdk:22
WORKDIR /app
COPY --from=build /build/target/MyTodoList-*.jar app.jar
COPY .env .
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]