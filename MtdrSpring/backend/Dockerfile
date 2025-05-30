# Build stage - using Amazon ECR Public (high limits)
FROM public.ecr.aws/docker/library/maven:3.8.6-amazoncorretto-11 as build
WORKDIR /build
COPY . .
RUN mvn clean package spring-boot:repackage

# Runtime stage - using Amazon ECR Public with Alpine + Node.js
FROM public.ecr.aws/amazoncorretto/amazoncorretto:11-alpine-jdk

# Install Node.js 18 on Alpine
RUN apk add --no-cache curl bash && \
    curl -fsSL https://unofficial-builds.nodejs.org/download/release/v18.20.2/node-v18.20.2-linux-x64-musl.tar.xz | tar -xJ -C /usr/local --strip-components=1 && \
    apk del curl

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
