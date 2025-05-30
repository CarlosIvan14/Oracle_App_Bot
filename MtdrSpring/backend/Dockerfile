FROM maven:3.8.6-amazoncorretto-11 as build
WORKDIR /build
COPY . .
RUN mvn clean package spring-boot:repackage

FROM eclipse-temurin:11-jre-alpine

RUN yum update -y && \
    curl -fsSL https://rpm.nodesource.com/setup_18.x | bash - && \
    yum install -y nodejs && \
    yum clean all

WORKDIR /app
COPY --from=build /build/target/MyTodoList-*.jar app.jar
COPY .env .
COPY src/main/frontend ./frontend

# Install frontend dependencies
WORKDIR /app/frontend
RUN npm install

WORKDIR /app
COPY --from=build /build/target/MyTodoList-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
