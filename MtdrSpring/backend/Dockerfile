# Build stage - Red Hat UBI with Maven
FROM registry.access.redhat.com/ubi8/openjdk-11:latest as build

USER root
RUN microdnf install -y maven
USER default

WORKDIR /build
COPY . .
RUN mvn clean package spring-boot:repackage

# Runtime stage - Red Hat UBI with Node.js
FROM registry.access.redhat.com/ubi8/openjdk-11-runtime:latest

USER root
# Install Node.js 18
RUN curl -fsSL https://rpm.nodesource.com/setup_18.x | bash - && \
    microdnf install -y nodejs && \
    microdnf clean all

WORKDIR /app
COPY --from=build /build/target/MyTodoList-*.jar app.jar
COPY .env .
COPY src/main/frontend ./frontend

# Install frontend dependencies
WORKDIR /app/frontend
RUN npm install

WORKDIR /app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]