# syntax=docker/dockerfile:1

ARG RUNTIME_IMAGE=eclipse-temurin:25-jre-alpine

# Stage 1 — builder
FROM eclipse-temurin:25-jdk-alpine AS builder

WORKDIR /build

COPY mvnw ./
COPY .mvn ./.mvn
COPY pom.xml ./

RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw package -DskipTests -B

# Stage 2 — runtime
FROM ${RUNTIME_IMAGE} AS runtime

WORKDIR /app

COPY --from=builder /build/target/flight-route-api-*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","/app/app.jar"]
