FROM maven:3.9.11-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src src
RUN mvn -B package -DskipTests
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/sensor-producer-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
